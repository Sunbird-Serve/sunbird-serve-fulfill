package com.sunbird.serve.fulfill.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000,https://serve-v1.evean.net}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        
        // Map to all paths including the context path
        registry.addMapping("/**")
                .allowedOriginPatterns(origins.toArray(new String[0]))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CorsInterceptor());
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOriginPatterns(origins);
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to all paths
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // Custom interceptor to ensure CORS headers are always set
    private class CorsInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            String origin = request.getHeader("Origin");
            if (origin != null && isAllowedOrigin(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
            }
            response.setHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials));
            response.setHeader("Access-Control-Allow-Methods", allowedMethods);
            response.setHeader("Access-Control-Allow-Headers", allowedHeaders);
            response.setHeader("Access-Control-Max-Age", String.valueOf(maxAge));
            
            // Handle preflight requests
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return false;
            }
            
            return true;
        }

        private boolean isAllowedOrigin(String origin) {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            return origins.stream().anyMatch(allowed -> 
                allowed.equals("*") || allowed.equals(origin) || 
                (allowed.startsWith("*.") && origin.endsWith(allowed.substring(1)))
            );
        }
    }
} 