package com.aiprep.interview.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OpenAiClient {

    private final RestClient restClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-flash-latest}")
    private String model;

    public OpenAiClient(@Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta/models}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    /**
     * Returns JSON output.
     */
    public JsonNode chatJson(String systemPrompt, String userPrompt) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new AiServiceException("GEMINI_API_KEY is not configured.");
        }

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of(
                                                "text",
                                                systemPrompt +
                                                        "\n\nIMPORTANT: Respond ONLY with valid JSON.\n\n"
                                                        + userPrompt
                                        )
                                }
                        )
                },
                // after
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "responseMimeType", "application/json",
                        "maxOutputTokens", 4096,
                        "thinkingConfig", Map.of("thinkingBudget", 0)

                )
        );

        String rawResponse;

        try {
            rawResponse = restClient.post()
                    .uri("/models/" + model + ":generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            throw new AiServiceException("Could not reach Gemini: " + e.getMessage(), e);
        }

        try {

            JsonNode root = mapper.readTree(rawResponse);

            String content = root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            return mapper.readTree(content);

        } catch (Exception e) {
            throw new AiServiceException("Failed to parse Gemini JSON response: " + e.getMessage(), e);
        }
    }

    /**
     * Returns plain text.
     */
    public String chatText(String systemPrompt, String userPrompt) {

        if (apiKey == null || apiKey.isBlank()) {
            throw new AiServiceException("GEMINI_API_KEY is not configured.");
        }

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of(
                                "parts", new Object[]{
                                        Map.of(
                                                "text",
                                                systemPrompt + "\n\n" + userPrompt
                                        )
                                }
                        )
                },
                "generationConfig", Map.of(
                        "temperature", 0.6
                )
        );

        String rawResponse;

        try {

            rawResponse = restClient.post()
                    .uri("/" + model + ":generateContent?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            throw new AiServiceException("Could not reach Gemini: " + e.getMessage(), e);
        }

        try {

            JsonNode root = mapper.readTree(rawResponse);

            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {
            throw new AiServiceException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }
}