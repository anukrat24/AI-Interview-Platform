package com.aiprep.interview.ai;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiResumeService {

    private final OpenAiClient openAiClient;

    public record ResumeAnalysisResult(
            double atsScore,
            List<String> missingKeywords,
            String weakPoints,
            String improvementSuggestions
    ) {}

    public ResumeAnalysisResult analyze(String resumeText, String targetRole) {
        String system = "You are an ATS (Applicant Tracking System) resume reviewer and career coach. "
                + "Always respond with valid JSON only, matching exactly this shape: "
                + "{\"atsScore\": number (0-100), \"missingKeywords\": [string], "
                + "\"weakPoints\": string, \"improvementSuggestions\": string}. "
                + "Do not include any text outside the JSON object.";

        String user = String.format(
                "Target role: %s%n%nResume text:%n%s%n%n"
                        + "Score this resume for ATS compatibility against the target role, "
                        + "list important keywords it is missing, point out weak or vague summary/bullet points, "
                        + "and give concrete improvement suggestions.",
                targetRole, truncate(resumeText, 12000)
        );

        JsonNode result = openAiClient.chatJson(system, user);

        List<String> missingKeywords = new ArrayList<>();
        for (JsonNode kw : result.path("missingKeywords")) {
            missingKeywords.add(kw.asText());
        }

        return new ResumeAnalysisResult(
                result.path("atsScore").asDouble(0),
                missingKeywords,
                result.path("weakPoints").asText(""),
                result.path("improvementSuggestions").asText("")
        );
    }

    private String truncate(String text, int maxChars) {
        return text.length() > maxChars ? text.substring(0, maxChars) : text;
    }
}
