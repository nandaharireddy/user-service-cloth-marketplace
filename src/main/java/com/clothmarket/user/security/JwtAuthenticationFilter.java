package com.clothmarket.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that parses and validates JWT Bearer tokens from incoming HTTP requests.
 * Authenticates the user in the Spring Security context for downstream controllers and services.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    /**
     * Inspects incoming request headers, validates JWT tokens, and populates SecurityContext.
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain filter execution chain
     * @throws ServletException on servlet errors
     * @throws IOException      on I/O errors
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length()).trim();

            if (jwtService.isTokenValid(token)) {
                Long userId = jwtService.extractUserId(token);
                String role = jwtService.extractRole(token);
                Long vendorId = jwtService.extractVendorId(token);

                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserPrincipal principal = UserPrincipal.builder()
                            .userId(userId)
                            .email(null) // Available if needed
                            .role(role)
                            .vendorId(vendorId)
                            .build();

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("Authenticated user id: {} with role: {} on path: {}", userId, role, request.getRequestURI());
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
