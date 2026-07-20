package com.aiprep.interview.service;

import com.aiprep.interview.dto.RoadmapRequestDTO;
import com.aiprep.interview.dto.RoadmapResponseDTO;

import java.util.List;

public interface RoadmapService {
    RoadmapResponseDTO generate(String userEmail, RoadmapRequestDTO request);
    List<RoadmapResponseDTO> getHistory(String userEmail);
}
