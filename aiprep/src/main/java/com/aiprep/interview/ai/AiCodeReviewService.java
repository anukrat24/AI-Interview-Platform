package com.aiprep.interview.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiCodeReviewService {

    private final OpenAiClient openAiClient;

    public String reviewCode(String language, String sourceCode, String executionOutput, String problemTitle) {
        String system = "You are a senior software engineer doing a code review in a technical interview context. "
                + "Be concise, specific, and constructive. Comment on correctness, time/space complexity, "
                + "edge cases, and code style. Respond in plain text (a few short paragraphs or a short bullet list), "
                + "not JSON.";

        String user = String.format(
                "Problem: %s%nLanguage: %s%n%nCode:%n%s%n%nExecution output:%n%s%n%n"
                        + "Review this submission as an interviewer would.",
                (problemTitle == null || problemTitle.isBlank()) ? "(ad-hoc practice snippet)" : problemTitle,
                language, sourceCode, executionOutput
        );

        return openAiClient.chatText(system, user);
    }
}
