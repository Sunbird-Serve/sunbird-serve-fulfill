package com.sunbird.serve.fulfill.VolunteeringService.services;

import com.sunbird.serve.fulfill.FulfillmentRepository;
import com.sunbird.serve.fulfill.NominationMapper;
import com.sunbird.serve.fulfill.exception.ServiceException;
import com.sunbird.serve.fulfill.models.Nomination.Fulfillment;
import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import com.sunbird.serve.fulfill.models.request.FulfillmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FulfillmentServiceTest {

    @Mock
    private FulfillmentRepository fulfillmentRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private FulfillmentService fulfillmentService;

    private FulfillmentRequest testFulfillmentRequest;
    private Fulfillment testFulfillment;

    @BeforeEach
    void setUp() {
        testFulfillmentRequest = FulfillmentRequest.builder()
                .needId("test-need-id")
                .needPlanId("test-plan-id")
                .assignedUserId("test-assigned-user")
                .coordUserId("test-coord-user")
                .fulfillmentStatus(FulfillmentStatus.InProgress)
                .build();

        testFulfillment = Fulfillment.builder()
                .id(UUID.randomUUID())
                .needId("test-need-id")
                .needPlanId("test-plan-id")
                .assignedUserId("test-assigned-user")
                .coordUserId("test-coord-user")
                .fulfillmentStatus(FulfillmentStatus.InProgress)
                .build();
    }

    @Test
    void createFulfillment_Success() {
        // Arrange
        when(fulfillmentRepository.save(any(Fulfillment.class))).thenReturn(testFulfillment);

        // Act
        Fulfillment result = fulfillmentService.createFulfillment(testFulfillmentRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testFulfillment.getNeedId(), result.getNeedId());
        assertEquals(testFulfillment.getAssignedUserId(), result.getAssignedUserId());
        verify(fulfillmentRepository, times(1)).save(any(Fulfillment.class));
    }

    @Test
    void createFulfillment_WhenRepositoryThrowsException_ShouldThrowServiceException() {
        // Arrange
        when(fulfillmentRepository.save(any(Fulfillment.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, 
                () -> fulfillmentService.createFulfillment(testFulfillmentRequest));
        
        assertEquals("Failed to create fulfillment", exception.getMessage());
        assertEquals("FULFILLMENT_CREATION_ERROR", exception.getErrorCode());
        assertEquals("fulfillment-service", exception.getServiceName());
    }

    @Test
    void getFulfillmentForAssignedUser_Success() {
        // Arrange
        String assignedUserId = "test-user";
        when(fulfillmentRepository.findAllByAssignedUserId(assignedUserId))
                .thenReturn(java.util.List.of(testFulfillment));

        // Act
        var result = fulfillmentService.getFulfillmentForAssignedUser(assignedUserId, 0, 10, null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testFulfillment.getAssignedUserId(), result.get(0).getAssignedUserId());
        verify(fulfillmentRepository, times(1)).findAllByAssignedUserId(assignedUserId);
    }

    @Test
    void getFulfillmentForAssignedUser_WhenRepositoryThrowsException_ShouldThrowServiceException() {
        // Arrange
        String assignedUserId = "test-user";
        when(fulfillmentRepository.findAllByAssignedUserId(assignedUserId))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        ServiceException exception = assertThrows(ServiceException.class, 
                () -> fulfillmentService.getFulfillmentForAssignedUser(assignedUserId, 0, 10, null));
        
        assertEquals("Failed to get fulfillment for assigned user", exception.getMessage());
        assertEquals("FULFILLMENT_RETRIEVAL_ERROR", exception.getErrorCode());
    }
} 