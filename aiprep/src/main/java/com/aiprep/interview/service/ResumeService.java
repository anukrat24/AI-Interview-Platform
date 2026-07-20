package com.aiprep.interview.service;

import com.aiprep.interview.dto.ResumeAnalysisDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ResumeService {
    ResumeAnalysisDTO analyzeResume(String userEmail, MultipartFile file, String targetRole) throws IOException;
    List<ResumeAnalysisDTO> getHistory(String userEmail);
}
