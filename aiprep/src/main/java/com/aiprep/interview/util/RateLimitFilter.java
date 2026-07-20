package com.aiprep.interview.util;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Basic protection against brute-forcing login/register/forgot-password.
 * Not a replacement for a real gateway-level rate limiter in a large-scale
 * deployment, but stops naive scripted abuse for a single-instance deployment.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Set<String> LIMITED_PATHS = Set.of(
            "/api/users/login", "/api/users/register", "/api/users/forgot-password"
    );
    private static final int MAX_REQUESTS_PER_WINDOW = 10;

    private final Cache<String, AtomicInteger> counters = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        if (LIMITED_PATHS.contains(request.getRequestURI())) {
            String key = clientIp(request) + ":" + request.getRequestURI();
            AtomicInteger count = counters.get(key, k -> new AtomicInteger(0));
            if (count.incrementAndGet() > MAX_REQUESTS_PER_WINDOW) {
                response.setStatus(429); // HTTP 429 Too Many Requests — HttpServletResponse has no SC_ constant for it
                response.setContentType("application/json");
                response.getWriter().write("{\"message\":\"Too many requests. Please try again in a minute.\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank()) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
    }
}
