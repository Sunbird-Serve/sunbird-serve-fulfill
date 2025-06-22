package com.sunbird.serve.fulfill.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private ServletWebRequest webRequest;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        webRequest = new ServletWebRequest(request);
    }

    @Test
    void handleServiceException_ShouldReturnInternalServerError() {
        // Arrange
        ServiceException serviceException = new ServiceException("Test service error", "TEST_ERROR", "test-service");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleServiceException(serviceException, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Service Error", response.getBody().getError());
        assertEquals("Test service error", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
    }

    @Test
    void handleExternalServiceException_ShouldReturnCorrectStatus() {
        // Arrange
        ExternalServiceException externalException = new ExternalServiceException(
            "External service error", 
            "EXTERNAL_ERROR", 
            "external-service", 
            HttpStatus.BAD_GATEWAY, 
            "http://test-service.com"
        );

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleExternalServiceException(externalException, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("External Service Error", response.getBody().getError());
        assertEquals("External service error", response.getBody().getMessage());
        assertEquals(502, response.getBody().getStatus());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        Exception genericException = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(genericException, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals(500, response.getBody().getStatus());
    }
} 