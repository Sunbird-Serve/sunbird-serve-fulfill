package com.sunbird.serve.fulfill;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.sunbird.serve.fulfill.models.Nomination.Nomination;
import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import com.sunbird.serve.fulfill.models.enums.NeedStatus;
import com.sunbird.serve.fulfill.models.request.NominationRequest;
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
public class NominationService {

    private final NominationRepository nominationRepository;
    private final WebClient webClient;
    private final NeedPlanRequest needPlanRequest;

    @Autowired
    public NominationService(NominationRepository nominationRepository, 
    WebClient.Builder webClientBuilder, NeedPlanRequest needPlanRequest) {
        this.nominationRepository = nominationRepository;
        this.webClient = webClientBuilder.baseUrl("http://localhost:9000").build();
        this.needPlanRequest = needPlanRequest;
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
        //need.setStatus(NeedStatus.valueOf(status.name()));
        //needRepository.save(need);
        // Call the createNeedPlan API
        callCreateNeedPlanApi(needPlanRequest,nomination.getNeedId(), headers);
        return nominationRepository.save(nomination);
    }

    private void callCreateNeedPlanApi(NeedPlanRequest request, String needId, Map<String, String> headers) {
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
        request.setAssignedUserId(needRequest.getUserId());
        request.setName(needRequest.getName());
        request.setStatus(NeedStatus.Approved);
        request.setOccurrenceId(needRequirementRequest.getOccurrenceId());

        webClient.post()
                .uri(apiUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .body(Mono.just(request), NeedPlanRequest.class)
                .retrieve()
                .toBodilessEntity()
                .block(); 
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
