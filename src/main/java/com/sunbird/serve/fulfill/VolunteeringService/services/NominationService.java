package com.sunbird.serve.fulfill.VolunteeringService.services;

import com.sunbird.serve.fulfill.NominationMapper;
import com.sunbird.serve.fulfill.VolunteeringService.repositories.NominationRepository;
import com.sunbird.serve.fulfill.models.needresponse.NeedResponse;
import com.sunbird.serve.fulfill.models.userresponse.UserResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.sunbird.serve.fulfill.models.Nomination.Nomination;
import com.sunbird.serve.fulfill.models.Need.Need;
import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import com.sunbird.serve.fulfill.models.enums.NeedStatus;
import com.sunbird.serve.fulfill.models.request.NominationRequest;
import com.sunbird.serve.fulfill.models.request.NeedPlanRequest;
import com.sunbird.serve.fulfill.models.request.NeedRequest;
import com.sunbird.serve.fulfill.models.request.UserStatusRequest;
import com.sunbird.serve.fulfill.models.Need.NeedPlan;
import com.sunbird.serve.fulfill.models.request.FulfillmentRequest;
import com.sunbird.serve.fulfill.models.request.NeedRequirementRequest;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.BodyInserters;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
public class NominationService {

    private final NominationRepository nominationRepository;
    private final WebClient webClient;
    private final NeedPlanRequest needPlanRequest;
    private final FulfillmentRequest fulfillmentRequest;
    private final FulfillmentService fulfillmentService;
    private final String serveUrl;
    private final String serveNeedUrl;
    private final String serveVolunteeringUserUrl;
    private final RestTemplate restTemplate;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailTemplateService emailTemplateService;

    private static final Logger logger = LoggerFactory.getLogger(NominationService.class);

    @Autowired
    public NominationService(NominationRepository nominationRepository,
                             WebClient.Builder webClientBuilder, NeedPlanRequest needPlanRequest,
                             FulfillmentRequest fulfillmentRequest,
                             FulfillmentService fulfillmentService, RestTemplate restTemplate, 
                             @Value("${serve.url}") String serveUrl,
                             @Value("${serve.need.url}") String serveNeedUrl,
                             @Value("${serve.volunteering.url}") String serveVolunteeringUserUrl) {
        this.nominationRepository = nominationRepository;
        this.webClient = webClientBuilder.baseUrl(serveUrl).build();
        this.needPlanRequest = needPlanRequest;
        this.fulfillmentRequest = fulfillmentRequest;
        this.fulfillmentService = fulfillmentService;
        this.restTemplate = restTemplate;
        this.serveUrl = serveUrl;
        this.serveNeedUrl = serveNeedUrl;
        this.serveVolunteeringUserUrl = serveVolunteeringUserUrl;
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public Nomination nominateNeed(NominationRequest nominationRequest) {
        try {
            // Convert NominationRequest to Nomination entity
            Nomination nomination = NominationMapper.mapToEntity(nominationRequest);
            Map<String, String> headers = new HashMap<>(); 
            String status = nominationRequest.getStatus().toString();
            String apiNeedUrl = String.format("%s/api/v1/serve-need/need/status/%s?status=%s", serveNeedUrl, nomination.getNeedId(), status);
            ResponseEntity<Need> responseEntity = webClient.put()
                    .uri(apiNeedUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                    .exchangeToMono(response -> response.toEntity(Need.class))
                    .block();
            // Save the entity
            return nominationRepository.save(nomination);
        } catch (Exception e) {
            logger.error("Failed to nominate need: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to nominate need", e);
        }
    }

    // Update nomination as confirm or reject
    public Nomination updateNomination(String userId, String nominationId, NominationStatus status, Map<String, String> headers) {
        Nomination nomination = nominationRepository.findById(UUID.fromString(nominationId)).get();
        //Need need = needRepository.findById(UUID.fromString(nomination.getNeedId())).get();
        UserStatusRequest userStatusRequest = new UserStatusRequest();
            nomination.setNominationStatus(status);
            List<Nomination> nominationList = getAllNominations(nomination.getNeedId(), headers);
        String needStatus = "";

            if (status.equals(NominationStatus.Approved)) {
        /*for (Nomination n : nominationList) {
            if (!n.getId().equals(nomination.getId())) {
                n.setNominationStatus(NominationStatus.Rejected);
                nominationRepository.save(n);
            }
        }*/
        needStatus = "Assigned";
        userStatusRequest.setStatus("Active");
        userStatusRequest.setSend(true);
        String apiUserUrl = String.format("%s/api/v1/serve-volunteering/user/status/update/%s",serveVolunteeringUserUrl, userId);
        webClient.put()
            .uri(apiUserUrl)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .headers(httpHeaders -> headers.forEach(httpHeaders::set))
            .body(BodyInserters.fromValue(userStatusRequest))
            .retrieve()
            .bodyToMono(Void.class)
            .block();
    } else if (status.equals(NominationStatus.Rejected)) {
        // Check if any other nomination is approved
        boolean anyApproved = nominationList.stream()
                .anyMatch(n -> n.getNominationStatus().equals(NominationStatus.Approved));
        boolean anyNominated = nominationList.stream()
                .anyMatch(n -> n.getNominationStatus().equals(NominationStatus.Nominated));

        if (anyApproved) {
            needStatus = "Assigned";
        } else if (anyNominated) {
            needStatus = "Nominated";
        } else {
            needStatus = "Approved";
        }
    }

        String apiNeedUrl = String.format("%s/api/v1/serve-need/need/status/%s?status=%s", serveNeedUrl, nomination.getNeedId(),needStatus);
        ResponseEntity<Need> responseEntity = webClient.put()
                    .uri(apiNeedUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                    .exchangeToMono(response -> response.toEntity(Need.class))
                    .block();

        // Call the createNeedPlan API
        String needPlanId = callCreateNeedPlanApi(needPlanRequest,nomination.getNeedId(), headers);

        // Create Fulfillment Details for this need
        createFulfillmentDetails(fulfillmentRequest, nomination.getNeedId(), needPlanId, nomination.getNominatedUserId(), headers);

        return nominationRepository.save(nomination);
    }

    private String callCreateNeedPlanApi(NeedPlanRequest request, String needId,
    Map<String, String> headers) {
        try{
            String apiNeedUrl = serveNeedUrl+"/api/v1/serve-need/need/"+needId;
            String apiUrl = serveNeedUrl+"/api/v1/serve-need/need-plan/create";
            String apiNeedReqUrl = serveNeedUrl+"/api/v1/serve-need/need-requirement/";

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

            // Extract the newly created Need Plan ID from the response
            String needPlanId = null;
            if (responseEntity != null && responseEntity.getBody() != null) {
                needPlanId = responseEntity.getBody().getId().toString();
            }

            return needPlanId;
        }catch (Exception e) {
            logger.error("Failed to get need plan created from fulfill ms: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create need plan from fulfill ms", e);
        }
    }


     private void createFulfillmentDetails(FulfillmentRequest request, String needId,
     String needPlanId,
     String assignedUserId,
     Map<String, String> headers) {
        try{
            String apiNeedUrl = serveNeedUrl+"/api/v1/serve-need/need/"+needId;
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
        }catch (Exception e) {
            logger.error("Failed to create fulfillment: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create fulfillment", e);
        }
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

    public void fetchNCoordinatorEmail(String needId, String nominatedUserId) {
    CompletableFuture<NeedResponse> needResponseFuture = fetchNeedResponseAsync(needId);
    CompletableFuture<UserResponse> nCoordinatorResponseFuture = needResponseFuture.thenCompose(needResponse ->
            fetchUserResponseAsync(needResponse.getUserId()));
    CompletableFuture<UserResponse> nominatedUserResponseFuture = fetchUserResponseAsync(nominatedUserId);

    CompletableFuture.allOf(needResponseFuture, nCoordinatorResponseFuture, nominatedUserResponseFuture)
            .thenRun(() -> {
                try {
                    NeedResponse needResponse = needResponseFuture.get();
                    UserResponse nCoordinatorResponse = nCoordinatorResponseFuture.get();
                    UserResponse nominatedUserResponse = nominatedUserResponseFuture.get();

                    sendEmailToNCoordinatorAsync(
                            nCoordinatorResponse.getIdentityDetails().getFullname(),
                            nCoordinatorResponse.getContactDetails().getEmail(),
                            needResponse.getDescription(),
                            nominatedUserResponse.getIdentityDetails().getFullname()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
}

    public void fetchNominatedUserEmail(String nominatedUserId) {
    fetchUserResponseAsync(nominatedUserId).thenAccept(userResponse -> {
        sendEmailToNominatedUserAsync(
                userResponse.getIdentityDetails().getFullname(),
                userResponse.getContactDetails().getEmail()
        );
    });
}

    public CompletableFuture<NeedResponse> fetchNeedResponseAsync(String needId) {
        return CompletableFuture.supplyAsync(() -> fetchNeedResponse(needId));
    }

    public CompletableFuture<UserResponse> fetchUserResponseAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> fetchUserResponse(userId));
    }

    private NeedResponse fetchNeedResponse(String needId) {
        String serveNeedApi = serveNeedUrl+"/api/v1/serve-need/need/" + needId;
        ResponseEntity<NeedResponse> responseEntity = restTemplate.getForEntity(serveNeedApi, NeedResponse.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new HttpStatusCodeException(responseEntity.getStatusCode(), "Failed to fetch need response") {};
        }
        return responseEntity.getBody();
    }

    private UserResponse fetchUserResponse(String userId) {
        String serveVolunteeringApi = serveVolunteeringUserUrl+"/api/v1/serve-volunteering/user/" + userId;
        ResponseEntity<UserResponse> responseEntity = restTemplate.getForEntity(serveVolunteeringApi, UserResponse.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new HttpStatusCodeException(responseEntity.getStatusCode(), "Failed to fetch user response") {};
        }
        return responseEntity.getBody();
    }

    @Async
    public CompletableFuture<Void> sendEmailToNCoordinatorAsync(String nCoordinatorName, String ncoordinatorEmail, String description, String nominatedUserName){
        try {
            sendEmailToNCoordinator(nCoordinatorName, ncoordinatorEmail, description, nominatedUserName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public void sendEmailToNCoordinator(String nCoordinatorName, String ncoordinatorEmail, String description, String nominatedUserName) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            String subject = emailTemplateService.getNCoordinatorEmailSubject();
            String body = emailTemplateService.getNCoordinatorEmailBody(nCoordinatorName, nominatedUserName, description);
            mimeMessageHelper.setTo(ncoordinatorEmail);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    @Async
    public CompletableFuture<Void> sendEmailToNominatedUserAsync(String nominatedUserName, String nominatedUserEmail) {
        try {
            sendEmailToNominatedUser(nominatedUserName, nominatedUserEmail);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public void sendEmailToNominatedUser(String nominatedUserName, String nominatedUserEmail) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            String subject = emailTemplateService.getNominatedUserEmailSubject();
            String body = emailTemplateService.getNominatedUserEmailBody(nominatedUserName);
            mimeMessageHelper.setTo(nominatedUserEmail);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }



    @Async
    public CompletableFuture<Void> sendEmailToVolunteerAsync(String nominatedUserId, NominationStatus status, String needId) {
        try {
            sendEmailToVolunteer(nominatedUserId, status, needId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public void sendEmailToVolunteer(String nominatedUserId, NominationStatus status, String needId) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            UserResponse userResponse = fetchUserResponse(nominatedUserId);
            String nominatedUserEmail = userResponse.getContactDetails().getEmail();
            String nominatedUserName = userResponse.getIdentityDetails().getFullname();
            NeedResponse needResponse = fetchNeedResponse(needId);
            String description = needResponse.getDescription();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            String subject = emailTemplateService.getVolunteerEmailSubject(status);
            String body = emailTemplateService.getVolunteerEmailBody(nominatedUserName, status, description);

            mimeMessageHelper.setTo(nominatedUserEmail);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    public List<UserResponse> getRecommendedVolunteersNotNominated(Map<String, String> headers) {
        UserResponse[] allVolunteers = webClient.get()
                .uri("/api/v1/serve-volunteering/user/status?status=Recommended")
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .retrieve()
                .bodyToMono(UserResponse[].class)
                .block();

        if (allVolunteers == null) {
            log.warn("API returned null response body");
            return new ArrayList<>();
        }

        List<UserResponse> notNominatedVolunteers = new ArrayList<>();

        for (UserResponse user : allVolunteers) {
            if (user.getRole() != null && user.getRole().contains("Volunteer")) {
                List<Nomination> nominations = nominationRepository.findAllByNominatedUserId(user.getOsid());
                if (nominations == null || nominations.isEmpty()) {
                    notNominatedVolunteers.add(user);
                }
            }
        }

        if (notNominatedVolunteers.isEmpty()) {
            log.info("All users from the API are already nominated.");
        }

        return notNominatedVolunteers;
    }


    public List<UserResponse> getRecommendedVolunteersNominated(Map<String, String> headers) {
        UserResponse[] allVolunteers = webClient.get()
                .uri("/api/v1/serve-volunteering/user/status?status=Recommended")
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .retrieve()
                .bodyToMono(UserResponse[].class)
                .block();

        if (allVolunteers == null) {
            log.warn("API returned null response body");
            return new ArrayList<>();
        }

        List<UserResponse> nominatedVolunteers = new ArrayList<>();

        for (UserResponse user : allVolunteers) {
            if (user.getRole() != null && user.getRole().contains("Volunteer")) {
                List<Nomination> nominations = nominationRepository.findAllByNominatedUserId(user.getOsid());
                if (nominations != null && !nominations.isEmpty()) {
                    nominatedVolunteers.add(user);
                }
            }
        }

        if (nominatedVolunteers.isEmpty()) {
            log.info("No recommended volunteers are present in the nomination table.");
        }

        return nominatedVolunteers;
    }

}
