package com.aiprep.interview.service;

import com.aiprep.interview.ai.AiRoadmapService;
import com.aiprep.interview.dto.RoadmapRequestDTO;
import com.aiprep.interview.dto.RoadmapResponseDTO;
import com.aiprep.interview.entity.RoadmapPlan;
import com.aiprep.interview.entity.User;
import com.aiprep.interview.repository.RoadmapPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapPlanRepository roadmapPlanRepository;
    private final UserService userService;
    private final AiRoadmapService aiRoadmapService;

    @Override
    @Transactional
    public RoadmapResponseDTO generate(String userEmail, RoadmapRequestDTO request) {
        User user = userService.getUserByEmail(userEmail);

        String plan = aiRoadmapService.generateRoadmap(
                request.getCurrentSkills(), request.getTargetRole(), request.getTargetCompany());

        RoadmapPlan roadmapPlan = new RoadmapPlan();
        roadmapPlan.setUser(user);
        roadmapPlan.setCurrentSkills(request.getCurrentSkills());
        roadmapPlan.setTargetRole(request.getTargetRole());
        roadmapPlan.setTargetCompany(request.getTargetCompany());
        roadmapPlan.setGeneratedPlan(plan);
        roadmapPlanRepository.save(roadmapPlan);

        return new RoadmapResponseDTO(roadmapPlan.getId(), plan);
    }

    @Override
    public List<RoadmapResponseDTO> getHistory(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        return roadmapPlanRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(r -> new RoadmapResponseDTO(r.getId(), r.getGeneratedPlan()))
                .toList();
    }
}
