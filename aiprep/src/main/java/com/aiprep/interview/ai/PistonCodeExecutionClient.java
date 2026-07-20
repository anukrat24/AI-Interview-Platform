package com.aiprep.interview.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Wraps the public Piston code execution API (https://github.com/engineer-man/piston).
 * We deliberately do NOT execute user-submitted code directly on our own server -
 * running arbitrary code from end users needs real sandboxing (containers, resource
 * limits, no network access), which Piston already provides as a dedicated service.
 * For very high traffic, self-host a Piston instance instead of using the public one.
 */
@Component
public class PistonCodeExecutionClient {

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();

    private final Cache<String, String> versionCache = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    public PistonCodeExecutionClient(@Value("${piston.base-url:https://emkc.org/api/v2/piston}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public record ExecutionResult(boolean success, String output) {}

    public ExecutionResult execute(String language, String sourceCode, String stdin) {
        String pistonLanguage = normalizeLanguage(language);
        String version = resolveVersion(pistonLanguage);

        Map<String, Object> body = Map.of(
                "language", pistonLanguage,
                "version", version,
                "files", List.of(Map.of("content", sourceCode)),
                "stdin", stdin == null ? "" : stdin
        );

        try {
            String raw = restClient.post()
                    .uri("/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = mapper.readTree(raw);
            JsonNode run = root.path("run");
            String stdout = run.path("stdout").asText("");
            String stderr = run.path("stderr").asText("");
            int exitCode = run.path("code").asInt(-1);

            String combinedOutput = stdout + (stderr.isBlank() ? "" : "\n--- stderr ---\n" + stderr);
            return new ExecutionResult(exitCode == 0, combinedOutput.isBlank() ? "(no output)" : combinedOutput);
        } catch (Exception e) {
            return new ExecutionResult(false, "Code execution service is unavailable right now: " + e.getMessage());
        }
    }

    private String normalizeLanguage(String language) {
        String l = language.trim().toLowerCase();
        return switch (l) {
            case "js", "node", "nodejs" -> "javascript";
            case "c++" -> "cpp";
            default -> l; // java, python, javascript, cpp already match Piston's names
        };
    }

    private String resolveVersion(String pistonLanguage) {
        return versionCache.get(pistonLanguage, lang -> {
            try {
                String raw = restClient.get().uri("/runtimes").retrieve().body(String.class);
                JsonNode runtimes = mapper.readTree(raw);
                for (JsonNode runtime : runtimes) {
                    if (runtime.path("language").asText().equalsIgnoreCase(lang)
                            || runtime.path("aliases").toString().toLowerCase().contains(lang)) {
                        return runtime.path("version").asText("*");
                    }
                }
            } catch (Exception ignored) {
                // fall through to default below
            }
            return "*";
        });
    }
}
