package com.aiprep.interview.controller;

import com.aiprep.interview.dto.RoadmapRequestDTO;
import com.aiprep.interview.dto.RoadmapResponseDTO;
import com.aiprep.interview.service.RoadmapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roadmap")
@RequiredArgsConstructor
public class RoadmapController {

    private final RoadmapService roadmapService;

    @PostMapping
    public ResponseEntity<RoadmapResponseDTO> generate(
            @Valid @RequestBody RoadmapRequestDTO request, Authentication authentication) {
        return ResponseEntity.ok(roadmapService.generate(authentication.getName(), request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<RoadmapResponseDTO>> history(Authentication authentication) {
        return ResponseEntity.ok(roadmapService.getHistory(authentication.getName()));
    }
}
