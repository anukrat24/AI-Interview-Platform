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

    public OpenAiClient(
            @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
            String baseUrl) {

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
                                                systemPrompt
                                                        + "\n\nIMPORTANT: Respond ONLY with valid JSON.\n\n"
                                                        + userPrompt
                                        )
                                }
                        )
                },
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "responseMimeType", "application/json",
                        "maxOutputTokens", 4096
                )
        );

        String rawResponse;

        try {

            // TEMPORARY DIAGNOSTIC - does NOT print the actual API key
            System.out.println(
                    "GEMINI KEY CHECK: present="
                            + (apiKey != null && !apiKey.isBlank())
                            + ", length="
                            + (apiKey == null ? 0 : apiKey.length())
                            + ", prefix="
                            + (
                            apiKey != null && apiKey.length() >= 4
                                    ? apiKey.substring(0, 4)
                                    : "NONE"
                    )
            );

            rawResponse = restClient.post()
                    .uri("/models/" + model + ":generateContent")
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            throw new AiServiceException(
                    "Could not reach Gemini: " + e.getMessage(),
                    e
            );
        }

        try {

            JsonNode root = mapper.readTree(rawResponse);

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new AiServiceException(
                        "Gemini returned no candidates: " + rawResponse
                );
            }

            JsonNode parts = candidates.get(0)
                    .path("content")
                    .path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                throw new AiServiceException(
                        "Gemini returned no content: " + rawResponse
                );
            }

            String content = parts.get(0)
                    .path("text")
                    .asText();

            if (content == null || content.isBlank()) {
                throw new AiServiceException(
                        "Gemini returned empty content: " + rawResponse
                );
            }

            return mapper.readTree(content);

        } catch (AiServiceException e) {
            throw e;

        } catch (Exception e) {
            throw new AiServiceException(
                    "Failed to parse Gemini JSON response: " + e.getMessage(),
                    e
            );
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
                    .uri("/models/" + model + ":generateContent")
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            throw new AiServiceException(
                    "Could not reach Gemini: " + e.getMessage(),
                    e
            );
        }

        try {

            JsonNode root = mapper.readTree(rawResponse);

            JsonNode candidates = root.path("candidates");

            if (!candidates.isArray() || candidates.isEmpty()) {
                throw new AiServiceException(
                        "Gemini returned no candidates: " + rawResponse
                );
            }

            JsonNode parts = candidates.get(0)
                    .path("content")
                    .path("parts");

            if (!parts.isArray() || parts.isEmpty()) {
                throw new AiServiceException(
                        "Gemini returned no content: " + rawResponse
                );
            }

            return parts.get(0)
                    .path("text")
                    .asText();

        } catch (AiServiceException e) {
            throw e;

        } catch (Exception e) {
            throw new AiServiceException(
                    "Failed to parse Gemini response: " + e.getMessage(),
                    e
            );
        }
    }
}