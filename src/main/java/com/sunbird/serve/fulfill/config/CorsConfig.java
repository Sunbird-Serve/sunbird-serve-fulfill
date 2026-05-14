package com.sunbird.serve.fulfill.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000,https://serve-v1.evean.net}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:*,Authorization,Content-Type,X-Requested-With,Accept,Origin}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        try {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            
            // Map to all paths including the context path
            registry.addMapping("/**")
                    .allowedOriginPatterns(origins.toArray(new String[0]))
                    .allowedMethods(allowedMethods.split(","))
                    .allowedHeaders(allowedHeaders.split(","))
                    .allowCredentials(allowCredentials)
                    .maxAge(maxAge);
        } catch (Exception e) {
            // Fallback configuration if properties are not properly set
            registry.addMapping("/**")
                    .allowedOriginPatterns("http://localhost:3000", "https://serve-v1.evean.net")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                    .allowedHeaders("*", "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                    .allowCredentials(true)
                    .maxAge(3600);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        try {
            List<String> origins = Arrays.asList(allowedOrigins.split(","));
            configuration.setAllowedOriginPatterns(origins);
            configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
            configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
            configuration.setAllowCredentials(allowCredentials);
            configuration.setMaxAge(maxAge);
        } catch (Exception e) {
            // Fallback configuration
            configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "https://serve-v1.evean.net"));
            configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
            configuration.setAllowedHeaders(Arrays.asList("*", "Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin"));
            configuration.setAllowCredentials(true);
            //configuration.setMaxAge(3600);
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply to all paths
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
} 