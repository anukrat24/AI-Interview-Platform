package com.aiprep.interview.controller;

import com.aiprep.interview.dto.CodeExecutionRequestDTO;
import com.aiprep.interview.dto.CodeExecutionResultDTO;
import com.aiprep.interview.service.CodingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coding")
@RequiredArgsConstructor
public class CodingController {

    private final CodingService codingService;

    @PostMapping("/run")
    public ResponseEntity<CodeExecutionResultDTO> run(
            @Valid @RequestBody CodeExecutionRequestDTO request, Authentication authentication) {
        return ResponseEntity.ok(codingService.runCode(authentication.getName(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<CodeExecutionResultDTO>> history(Authentication authentication) {
        return ResponseEntity.ok(codingService.getHistory(authentication.getName()));
    }
}
