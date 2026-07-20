package com.aiprep.interview.service;

import com.aiprep.interview.dto.*;
import com.aiprep.interview.entity.User;
import com.aiprep.interview.repository.UserRepository;
import com.aiprep.interview.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public AuthResponse register(RegisterDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.USER);
        user.setAuthProvider(User.AuthProvider.LOCAL);
        userRepository.save(user);

        return new AuthResponse("Registration successful. Please log in.", null);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginDTO request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (user.isBanned()) {
            throw new IllegalArgumentException("This account has been suspended. Contact support.");
        }

        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException("This account uses Google sign-in. Please use 'Continue with Google'.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Step 1: no OTP provided yet -> generate one and email it
        if (request.getOtp() == null || request.getOtp().isBlank()) {
            String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
            user.setOtpCode(otp);
            user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
            userRepository.save(user);
            emailService.sendOtpEmail(user.getEmail(), otp);
            return new AuthResponse("OTP sent to your email. Please enter it to continue.", null);
        }

        // Step 2: verify OTP
        if (user.getOtpCode() == null || user.getOtpExpiry() == null
                || user.getOtpExpiry().isBefore(LocalDateTime.now())
                || !user.getOtpCode().equals(request.getOtp().trim())) {
            throw new IllegalArgumentException("Invalid or expired OTP. Please try logging in again.");
        }

        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), List.of("ROLE_" + user.getRole()));
        return new AuthResponse("Login successful", token);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordDTO request) {
        userRepository.findByEmail(request.getEmail().toLowerCase()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(30));
            userRepository.save(user);
            // Frontend base URL should match wherever this app is hosted.
            emailService.sendPasswordResetEmail(user.getEmail(), "/reset-password?token=" + token);
        });
        // Intentionally does not reveal whether the email exists, to avoid account enumeration.
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));

        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
