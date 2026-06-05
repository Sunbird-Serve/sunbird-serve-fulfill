package com.sunbird.serve.fulfill.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Extracts tenant/user information from the validated JWT
 * and populates TenantContext for use in business logic.
 * Runs after Spring Security authentication filter.
 */
@Component
public class JwtTenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();

                String agencyId = jwt.getClaimAsString("agencyId");
                String agencyType = jwt.getClaimAsString("agencyType");
                String userId = jwt.getSubject();
                String email = jwt.getClaimAsString("email");

                TenantContext.setAgencyId(agencyId);
                TenantContext.setAgencyType(agencyType);
                TenantContext.setUserId(userId);
                TenantContext.setUserEmail(email);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
