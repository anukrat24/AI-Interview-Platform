package com.aiprep.interview.controller;

import com.aiprep.interview.dto.*;
import com.aiprep.interview.entity.User;
import com.aiprep.interview.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${app.free-interviews-per-day:3}")
    private int freeInterviewsPerDay;

    @Value("${app.cookie-secure:true}")
    private boolean cookieSecure;


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterDTO request) {
        return ResponseEntity.ok(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDTO request, HttpServletResponse response) {
        AuthResponse authResponse = userService.login(request);

        if (authResponse.getToken() != null) {
            Cookie jwtCookie = new Cookie("jwt", authResponse.getToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60);
            jwtCookie.setSecure(cookieSecure);
            jwtCookie.setAttribute("SameSite", "Lax");
            response.addCookie(jwtCookie);
        }

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordDTO request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok(new AuthResponse(
                "If an account exists with that email, a reset link has been sent.", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(new AuthResponse("Password reset successful. Please log in.", null));
    }

    @GetMapping("/me")
    public ResponseEntity<MeDTO> me(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        return ResponseEntity.ok(new MeDTO(
                user.getName(), user.getEmail(), user.getRole().name(),
                user.getSubscriptionTier().name(), user.getInterviewsUsedToday(), freeInterviewsPerDay));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        jwtCookie.setSecure(cookieSecure);
        response.addCookie(jwtCookie);
        return ResponseEntity.ok(new AuthResponse("Logged out", null));
    }
}
