package com.clothmarket.user.security;

import com.clothmarket.user.dto.response.ErrorResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * In-memory sliding window rate limiting filter for authentication endpoints.
 * Protects {@code /auth/login} and {@code /auth/register} against brute-force and credential stuffing attacks.
 * Tracks incoming requests per client IP address over a 60-second sliding time window.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final int maxRequestsPerMinute;
    private final Map<String, ConcurrentLinkedQueue<Long>> requestTimestampsByIp = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    /**
     * Constructs the RateLimitingFilter with injected rate limit thresholds.
     *
     * @param maxRequestsPerMinute maximum requests allowed per minute per IP
     */
    public RateLimitingFilter(
            @Value("${security.rate-limit.max-requests-per-minute:10}") int maxRequestsPerMinute) {
        this.maxRequestsPerMinute = maxRequestsPerMinute;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Filters incoming HTTP requests and enforces rate limits on authentication endpoints.
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain filter chain
     * @throws ServletException on servlet error
     * @throws IOException      on I/O error
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();

        // Rate limit only sensitive authentication write endpoints
        if (isRateLimitedPath(uri, request.getMethod())) {
            String clientIp = extractClientIp(request);
            long now = Instant.now().toEpochMilli();
            long windowStart = now - 60_000; // 1 minute sliding window

            ConcurrentLinkedQueue<Long> timestamps =
                    requestTimestampsByIp.computeIfAbsent(clientIp, k -> new ConcurrentLinkedQueue<>());

            // Evict expired timestamps outside current window
            while (!timestamps.isEmpty() && timestamps.peek() < windowStart) {
                timestamps.poll();
            }

            if (timestamps.size() >= maxRequestsPerMinute) {
                log.warn("Rate limit exceeded for IP: {} on URI: {}. Total requests in window: {}",
                        clientIp, uri, timestamps.size());

                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setHeader("Retry-After", "60");

                ErrorResponseDto errorResponse = ErrorResponseDto.builder()
                        .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                        .status(HttpStatus.TOO_MANY_REQUESTS.value())
                        .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                        .message("Too many authentication attempts. Please wait 1 minute before retrying.")
                        .path(uri)
                        .build();

                objectMapper.writeValue(response.getOutputStream(), errorResponse);
                return;
            }

            timestamps.offer(now);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedPath(String uri, String method) {
        if (!"POST".equalsIgnoreCase(method)) {
            return false;
        }
        return uri.endsWith("/auth/login") || uri.endsWith("/auth/register");
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
