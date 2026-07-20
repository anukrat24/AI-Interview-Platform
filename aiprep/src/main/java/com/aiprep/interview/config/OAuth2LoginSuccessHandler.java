package com.aiprep.interview.config;

import com.aiprep.interview.entity.User;
import com.aiprep.interview.repository.UserRepository;
import com.aiprep.interview.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.cookie-secure:true}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name != null ? name : email);
            newUser.setAuthProvider(User.AuthProvider.GOOGLE);
            newUser.setRole(User.Role.USER);
            return userRepository.save(newUser);
        });

        if (user.isBanned()) {
            response.sendRedirect("/login?error=banned");
            return;
        }

        String token = jwtUtil.generateToken(user.getEmail(), List.of("ROLE_" + user.getRole()));

        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(24 * 60 * 60);
        jwtCookie.setSecure(cookieSecure);
        jwtCookie.setAttribute("SameSite", "Lax");
        response.addCookie(jwtCookie);

        response.sendRedirect("/dashboard");
    }
}
