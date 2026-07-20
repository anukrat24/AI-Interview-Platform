package com.aiprep.interview.controller;

import com.aiprep.interview.dto.AdminAnalyticsDTO;
import com.aiprep.interview.dto.AdminUserViewDTO;
import com.aiprep.interview.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserViewDTO>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<Void> ban(@PathVariable Long userId) {
        adminService.banUser(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<Void> unban(@PathVariable Long userId) {
        adminService.unbanUser(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsDTO> analytics() {
        return ResponseEntity.ok(adminService.getAnalytics());
    }
}
