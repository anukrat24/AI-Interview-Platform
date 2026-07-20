package com.aiprep.interview.service;

import com.aiprep.interview.ai.AiResumeService;
import com.aiprep.interview.dto.ResumeAnalysisDTO;
import com.aiprep.interview.entity.Resume;
import com.aiprep.interview.entity.User;
import com.aiprep.interview.repository.ResumeRepository;
import com.aiprep.interview.util.PdfTextExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserService userService;
    private final PdfTextExtractor pdfTextExtractor;
    private final AiResumeService aiResumeService;

    @Override
    @Transactional
    public ResumeAnalysisDTO analyzeResume(String userEmail, MultipartFile file, String targetRole) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please upload a PDF resume");
        }
        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        User user = userService.getUserByEmail(userEmail);
        String text = pdfTextExtractor.extractText(file.getInputStream());

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Could not extract any text from this PDF. It may be a scanned image - try a text-based PDF export instead.");
        }

        AiResumeService.ResumeAnalysisResult result = aiResumeService.analyze(text, targetRole);

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setOriginalFilename(file.getOriginalFilename());
        resume.setExtractedText(text);
        resume.setTargetRole(targetRole);
        resume.setAtsScore(result.atsScore());
        resume.setMissingKeywords(String.join(", ", result.missingKeywords()));
        resume.setWeakPoints(result.weakPoints());
        resume.setImprovementSuggestions(result.improvementSuggestions());
        resumeRepository.save(resume);

        return new ResumeAnalysisDTO(resume.getId(), result.atsScore(), result.missingKeywords(),
                result.weakPoints(), result.improvementSuggestions());
    }

    @Override
    public List<ResumeAnalysisDTO> getHistory(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        return resumeRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(r -> new ResumeAnalysisDTO(
                        r.getId(), r.getAtsScore(),
                        r.getMissingKeywords() == null || r.getMissingKeywords().isBlank()
                                ? List.of() : List.of(r.getMissingKeywords().split(",\\s*")),
                        r.getWeakPoints(), r.getImprovementSuggestions()))
                .toList();
    }
}
