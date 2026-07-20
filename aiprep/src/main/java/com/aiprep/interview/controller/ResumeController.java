package com.aiprep.interview.controller;

import com.aiprep.interview.dto.ResumeAnalysisDTO;
import com.aiprep.interview.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ResumeAnalysisDTO> analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "targetRole", defaultValue = "Software Engineer") String targetRole,
            Authentication authentication) throws IOException {
        return ResponseEntity.ok(resumeService.analyzeResume(authentication.getName(), file, targetRole));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ResumeAnalysisDTO>> history(Authentication authentication) {
        return ResponseEntity.ok(resumeService.getHistory(authentication.getName()));
    }
}
