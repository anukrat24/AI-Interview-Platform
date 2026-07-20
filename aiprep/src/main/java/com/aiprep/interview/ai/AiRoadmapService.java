package com.aiprep.interview.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiRoadmapService {

    private final OpenAiClient openAiClient;

    public String generateRoadmap(String currentSkills, String targetRole, String targetCompany) {
        String system = "You are a career coach who builds concrete, realistic study roadmaps for "
                + "software engineering job seekers. Respond in clean Markdown with headings, "
                + "a week-by-week or day-by-day plan, and a list of topics to prioritize. "
                + "Be specific and practical, not generic.";

        String companyPart = (targetCompany != null && !targetCompany.isBlank())
                ? " targeting " + targetCompany
                : "";

        String user = String.format(
                "Current skills: %s%nTarget role: %s%s%n%n"
                        + "Build a realistic preparation roadmap: identify skill gaps, list topics to study in "
                        + "priority order, and lay out a day-by-day or week-by-week plan for the next 4 weeks.",
                currentSkills, targetRole, companyPart
        );

        return openAiClient.chatText(system, user);
    }
}
