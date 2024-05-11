package com.sunbird.serve.fulfill;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.sunbird.serve.fulfill.models.Nomination.Fulfillment;
import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import com.sunbird.serve.fulfill.models.enums.NeedStatus;
import com.sunbird.serve.fulfill.models.request.FulfillmentRequest;
import com.sunbird.serve.fulfill.models.request.NeedPlanRequest;
import com.sunbird.serve.fulfill.models.request.NeedRequest;
import com.sunbird.serve.fulfill.models.request.NeedRequirementRequest;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;


@Service
public class FulfillmentService {

    private final FulfillmentRepository fulfillmentRepository;
    private final WebClient webClient;
    private final FulfillmentRequest fulfillmentRequest;

    @Autowired
    public FulfillmentService(FulfillmentRepository fulfillmentRepository, 
    WebClient.Builder webClientBuilder, FulfillmentRequest fulfillmentRequest) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:9000").build();
        this.fulfillmentRequest = fulfillmentRequest;
    }

    public Fulfillment createFulfillment(FulfillmentRequest fulfillmentRequest) {
        // Convert fulfillmentRequest to Fulfillment entity
        Fulfillment fulfillment = NominationMapper.mapToEntityFulfill(fulfillmentRequest);

        // Save the entity
        return fulfillmentRepository.save(fulfillment);
    }

    public List<Fulfillment> getFulfillmentForAssignedUser(String assignedUserId, Integer page, Integer size, Map<String, String> headers) {
        return fulfillmentRepository.findAllByAssignedUserId(assignedUserId);
    }

    public List<Fulfillment> getFulfillmentForCoordUser(String coordUserId, Integer page, Integer size, Map<String, String> headers) {
        return fulfillmentRepository.findAllByCoordUserId(coordUserId);
    }
}
