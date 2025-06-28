package com.sunbird.serve.fulfill.VolunteeringService.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.Map;
import java.util.HashMap;

@RestController
@CrossOrigin(origins = {"http://localhost:3000", "https://serve-v1.evean.net"}, allowCredentials = "true")
public class HealthController {

    @Operation(summary = "Health Check", description = "Simple health check endpoint to verify CORS configuration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy"),
            @ApiResponse(responseCode = "500", description = "Service is unhealthy")
    })
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "sunbird-serve-fulfill");
        response.put("timestamp", System.currentTimeMillis());
        response.put("cors", "enabled");
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "CORS Test", description = "Test endpoint to verify CORS headers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CORS test successful"),
            @ApiResponse(responseCode = "405", description = "Method not allowed")
    })
    @PostMapping("/cors-test")
    public ResponseEntity<Map<String, Object>> corsTest(@RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CORS test successful");
        response.put("receivedBody", body);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Options Test", description = "Test OPTIONS request for CORS preflight")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OPTIONS request handled")
    })
    @RequestMapping(value = "/options-test", method = RequestMethod.OPTIONS)
    public ResponseEntity<Void> optionsTest() {
        return ResponseEntity.ok().build();
    }
} 