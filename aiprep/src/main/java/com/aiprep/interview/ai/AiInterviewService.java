package com.aiprep.interview.ai;

import com.aiprep.interview.dto.FeedbackDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiInterviewService {

    private final OpenAiClient openAiClient;

    public record GeneratedQuestion(String questionText, String difficulty) {}

    public List<GeneratedQuestion> generateQuestions(String role, String experienceLevel,
                                                       String interviewType, int count) {
        String system = "You are an expert technical interviewer. Always respond with valid JSON only, "
                + "matching exactly this shape: {\"questions\": [{\"questionText\": string, \"difficulty\": \"EASY\"|\"MEDIUM\"|\"HARD\"}]}. "
                + "Do not include any text outside the JSON object.";

        String user = String.format(
                "Generate %d interview questions for a %s candidate applying for a %s role, "
                        + "focused on a %s interview. Vary the difficulty and make questions specific and realistic, "
                        + "not generic textbook definitions.",
                count, experienceLevel, role, interviewType
        );

        JsonNode result = openAiClient.chatJson(system, user);
        List<GeneratedQuestion> questions = new ArrayList<>();
        for (JsonNode q : result.path("questions")) {
            questions.add(new GeneratedQuestion(
                    q.path("questionText").asText(),
                    q.path("difficulty").asText("MEDIUM")
            ));
        }
        return questions;
    }

    public FeedbackDTO evaluateAnswer(String questionText, String answerText, String role) {
        String system = "You are a strict but constructive technical interview evaluator. "
                + "Always respond with valid JSON only, matching exactly this shape: "
                + "{\"scoreOutOf10\": number, \"technicalCorrectness\": number (0-10), "
                + "\"confidence\": number (0-10), \"communication\": number (0-10), "
                + "\"keywordCoverage\": number (0-10), \"suggestions\": string, \"sampleAnswer\": string}. "
                + "Do not include any text outside the JSON object.";

        String user = String.format(
                "Role: %s%nQuestion: %s%nCandidate answer: %s%n"
                        + "Evaluate the answer's technical correctness, confidence, communication clarity, "
                        + "and keyword coverage. Give concrete improvement suggestions and a better sample answer.",
                role, questionText, answerText
        );

        JsonNode result = openAiClient.chatJson(system, user);
        return new FeedbackDTO(
                result.path("scoreOutOf10").asDouble(0),
                result.path("technicalCorrectness").asDouble(0),
                result.path("confidence").asDouble(0),
                result.path("communication").asDouble(0),
                result.path("keywordCoverage").asDouble(0),
                result.path("suggestions").asText(""),
                result.path("sampleAnswer").asText("")
        );
    }
}
