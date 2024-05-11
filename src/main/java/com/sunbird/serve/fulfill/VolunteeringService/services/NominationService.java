package com.sunbird.serve.fulfill;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.sunbird.serve.fulfill.models.Nomination.Nomination;
import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import com.sunbird.serve.fulfill.models.enums.NeedStatus;
import com.sunbird.serve.fulfill.models.request.NominationRequest;
import com.sunbird.serve.fulfill.models.request.NeedPlanRequest;
import com.sunbird.serve.fulfill.models.request.NeedRequest;
import com.sunbird.serve.fulfill.models.request.NeedPlanResponse;
import com.sunbird.serve.fulfill.models.Need.NeedPlan;
import com.sunbird.serve.fulfill.models.request.FulfillmentRequest;
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
public class NominationService {

    private final NominationRepository nominationRepository;
    private final WebClient webClient;
    private final NeedPlanRequest needPlanRequest;
    private final FulfillmentRequest fulfillmentRequest;
    private final FulfillmentService fulfillmentService;

    @Autowired
    public NominationService(NominationRepository nominationRepository, 
    WebClient.Builder webClientBuilder, NeedPlanRequest needPlanRequest,
    FulfillmentRequest fulfillmentRequest,
    FulfillmentService fulfillmentService) {
        this.nominationRepository = nominationRepository;
        this.webClient = webClientBuilder.baseUrl("https://serve-v1.evean.net").build();
        this.needPlanRequest = needPlanRequest;
        this.fulfillmentRequest = fulfillmentRequest;
        this.fulfillmentService = fulfillmentService;
    }

    public Nomination nominateNeed(NominationRequest nominationRequest) {
        // Convert NominationRequest to Nomination entity
        Nomination nomination = NominationMapper.mapToEntity(nominationRequest);

        // Save the entity
        return nominationRepository.save(nomination);
    }

    //update nomination as confirm or reject
    public Nomination updateNomination(String userId, String nominationId, NominationStatus status, Map<String, String> headers) {
        Nomination nomination = nominationRepository.findById(UUID.fromString(nominationId)).get();
        //Need need = needRepository.findById(UUID.fromString(nomination.getNeedId())).get();

        nomination.setNominationStatus(status);
    
        // Call the createNeedPlan API
        String needPlanId = callCreateNeedPlanApi(needPlanRequest,nomination.getNeedId(), headers);

        // Create Fulfillment Details for this need
        createFulfillmentDetails(fulfillmentRequest, nomination.getNeedId(), needPlanId, nomination.getNominatedUserId(), headers);
        
        return nominationRepository.save(nomination);
    }

    private String callCreateNeedPlanApi(NeedPlanRequest request, String needId, 
    Map<String, String> headers) {
        String apiNeedUrl = "/api/v1/serve-need/need/"+needId;
        String apiUrl = "/api/v1/serve-need/need-plan/create";
        String apiNeedReqUrl = "/api/v1/serve-need/need-requirement/";

        // Get Need details
        NeedRequest needRequest = webClient.get()
            .uri(apiNeedUrl, needId)
            .headers(httpHeaders -> headers.forEach(httpHeaders::set))
            .retrieve()
            .bodyToMono(NeedRequest.class)
            .block(); 

        apiNeedReqUrl = apiNeedReqUrl+needRequest.getRequirementId();
        // Get Need Requirement details
        NeedRequirementRequest needRequirementRequest = webClient.get()
            .uri(apiNeedReqUrl, needRequest.getRequirementId())
            .headers(httpHeaders -> headers.forEach(httpHeaders::set))
            .retrieve()
            .bodyToMono(NeedRequirementRequest.class)
            .block(); 


        request.setNeedId(needId);
        request.setName(needRequest.getName());
        request.setStatus(NeedStatus.Approved);
        request.setOccurrenceId(needRequirementRequest.getOccurrenceId());

        ResponseEntity<NeedPlan> responseEntity = webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .body(Mono.just(request), NeedPlanRequest.class)
                .exchangeToMono(response -> response.toEntity(NeedPlan.class))
                .block(); 

        System.out.println("Response Body: " + responseEntity.getBody());

        // Extract the newly created Need Plan ID from the response
        String needPlanId = null;
        if (responseEntity != null && responseEntity.getBody() != null) {
            needPlanId = responseEntity.getBody().getId().toString();
        }
        
        return needPlanId;
    }


     private void createFulfillmentDetails(FulfillmentRequest request, String needId, 
     String needPlanId,
     String assignedUserId,
     Map<String, String> headers) {
       
        String apiNeedUrl = "/api/v1/serve-need/need/"+needId;
        // Get Need details
        NeedRequest needRequest = webClient.get()
            .uri(apiNeedUrl, needId)
            .headers(httpHeaders -> headers.forEach(httpHeaders::set))
            .retrieve()
            .bodyToMono(NeedRequest.class)
            .block(); 

        request.setNeedId(needId);
        request.setNeedPlanId(needPlanId);
        request.setAssignedUserId(assignedUserId);
        request.setCoordUserId(needRequest.getUserId());
        request.setFulfillmentStatus(FulfillmentStatus.InProgress);
        fulfillmentService.createFulfillment(request);
    }

    public List<Nomination> getAllNominations(String needId, Map<String, String> headers) {
        return nominationRepository.findAllByNeedId(needId);
    }

    public List<Nomination> getAllNominationsByStatus(String needId, NominationStatus status, Map<String, String> headers) {
        return nominationRepository.findAllByNeedIdAndNominationStatus(needId, status);
    }


    public List<Nomination> getAllNominationForUser(String nominatedUserId, Integer page, Integer size, Map<String, String> headers) {
        return nominationRepository.findAllByNominatedUserId(nominatedUserId);
    }

    // Add other service methods as needed
}
