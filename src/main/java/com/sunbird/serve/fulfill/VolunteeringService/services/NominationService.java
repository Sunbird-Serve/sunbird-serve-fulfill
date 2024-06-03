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
import com.sunbird.serve.fulfill.models.Need.NeedPlan;
import com.sunbird.serve.fulfill.models.request.FulfillmentRequest;
import com.sunbird.serve.fulfill.models.request.NeedRequirementRequest;
import java.util.List;

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

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;


@Service
@Slf4j
public class NominationService {

    private final NominationRepository nominationRepository;
    private final WebClient webClient;
    private final NeedPlanRequest needPlanRequest;
    private final FulfillmentRequest fulfillmentRequest;
    private final FulfillmentService fulfillmentService;

    private final RestTemplate restTemplate;


    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    public NominationService(NominationRepository nominationRepository,
                             WebClient.Builder webClientBuilder, NeedPlanRequest needPlanRequest,
                             FulfillmentRequest fulfillmentRequest,
                             FulfillmentService fulfillmentService, RestTemplate restTemplate) {
        this.nominationRepository = nominationRepository;
        this.webClient = webClientBuilder.baseUrl("https://serve-v1.evean.net").build();
        this.needPlanRequest = needPlanRequest;
        this.fulfillmentRequest = fulfillmentRequest;
        this.fulfillmentService = fulfillmentService;
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    public Nomination nominateNeed(NominationRequest nominationRequest) {
        // Convert NominationRequest to Nomination entity
        Nomination nomination = NominationMapper.mapToEntity(nominationRequest);
        Map<String, String> headers = new HashMap<>(); 
        String status = nominationRequest.getStatus().toString();
        String apiNeedUrl = String.format("/api/v1/serve-need/need/status/%s?status=%s", nomination.getNeedId(), status);
        ResponseEntity<Need> responseEntity = webClient.put()
                .uri(apiNeedUrl)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .headers(httpHeaders -> headers.forEach(httpHeaders::set))
                .exchangeToMono(response -> response.toEntity(Need.class))
                .block();
        // Save the entity
        return nominationRepository.save(nomination);
    }

    //update nomination as confirm or reject
    public Nomination updateNomination(String userId, String nominationId, NominationStatus status, Map<String, String> headers) {
        Nomination nomination = nominationRepository.findById(UUID.fromString(nominationId)).get();
        //Need need = needRepository.findById(UUID.fromString(nomination.getNeedId())).get();

        nomination.setNominationStatus(status);

        //String apiNeedUrl = "/api/v1/serve-need/need/status"+nomination.getNeedId()+"?status=Assigned";
        String apiNeedUrl = String.format("/api/v1/serve-need/need/status/%s?status=Assigned", nomination.getNeedId());
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

    /*public void fetchNCoordinatorEmail(String needId,String nominatedUserId) {

        try {
            NeedResponse needResponse = fetchNeedResponse(needId);
            String ncoordinatorUserId = needResponse.getUserId();
            String description = needResponse.getDescription();

            UserResponse userResponse = fetchUserResponse(ncoordinatorUserId);
            UserResponse nominatedUserResponse = fetchUserResponse(nominatedUserId);
            
            String nCoordinatorName = userResponse.getIdentityDetails().getFullname();
            String ncoordinatorEmail = userResponse.getContactDetails().getEmail();
            String nominatedUserName = nominatedUserResponse.getIdentityDetails().getFullname();

            sendEmailToNCoordinatorAsync(nCoordinatorName, ncoordinatorEmail, description, nominatedUserName);
        } catch (HttpStatusCodeException e) {
            log.info("Error fetching email: {}", e.getMessage());
        }

        }*/

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


    /*public void fetchNominatedUserEmail(String nominatedUserId) {

        try {
            UserResponse userResponse = fetchUserResponse(nominatedUserId);
            String nominatedUserEmail = userResponse.getContactDetails().getEmail();
            String nominatedUserName = userResponse.getIdentityDetails().getFullname();

            sendEmailToNominatedUserAsync(nominatedUserName,nominatedUserEmail);
        } catch (HttpStatusCodeException e) {
            log.info("Error fetching email: {}", e.getMessage());
        }

    }*/

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
        String serveNeedUrl = "https://serve-v1.evean.net/api/v1/serve-need/need/" + needId;
        ResponseEntity<NeedResponse> responseEntity = restTemplate.getForEntity(serveNeedUrl, NeedResponse.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new HttpStatusCodeException(responseEntity.getStatusCode(), "Failed to fetch need response") {};
        }
        return responseEntity.getBody();
    }

    private UserResponse fetchUserResponse(String userId) {
        String serveVolunteeringUserUrl = "https://serve-v1.evean.net/api/v1/serve-volunteering/user/" + userId;
        ResponseEntity<UserResponse> responseEntity = restTemplate.getForEntity(serveVolunteeringUserUrl, UserResponse.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new HttpStatusCodeException(responseEntity.getStatusCode(), "Failed to fetch user response") {};
        }
        return responseEntity.getBody();
    }

    @Async
    public CompletableFuture<Void> sendEmailToNCoordinatorAsync(String nCoordinatorName, String ncoordinatorEmail, String description, String nominatedUserName){
        try {
            sendEmailToNCoordinator(nCoordinatorName, ncoordinatorEmail, description, nominatedUserName);
        } catch (HttpStatusCodeException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public void sendEmailToNCoordinator(String nCoordinatorName, String ncoordinatorEmail, String description, String nominatedUserName) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            mimeMessageHelper.setTo(ncoordinatorEmail);
            mimeMessageHelper.setSubject("New Volunteer Need Nomination: Action Required");
            mimeMessageHelper.setText("Dear " + nCoordinatorName + ",<br>" +
                    "<br>" +
                    "This is to bring to your attention a new volunteer need that has been nominated by one of our dedicated volunteers through the SERVE platform." +
                    "<br><br>"+
                    "Volunteer Name: " + nominatedUserName +
                    "<br><br>" +
                    "Nominated Need: " + description +
                    "<br><br>" +
                    "Please take a moment to review the nominated need and provide your feedback or decision. Your prompt attention to this matter is greatly appreciated." +
                    "<br>" +
                    "Thank you for your continued dedication to our mission and for your support in making SERVE a platform that truly makes a difference in people's lives." +
                    "<br>" +
                    "<br>" +
                    "Warm Regards," +
                    "<br>" +
                    "Admin", true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public CompletableFuture<Void> sendEmailToNominatedUserAsync(String nominatedUserName, String nominatedUserEmail) {
        try {
            sendEmailToNominatedUser(nominatedUserName, nominatedUserEmail);
        } catch (HttpStatusCodeException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public void sendEmailToNominatedUser(String nominatedUserName, String nominatedUserEmail) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            mimeMessageHelper.setTo(nominatedUserEmail);
            mimeMessageHelper.setSubject("Your Volunteer Need Nomination - Thank You!");
            mimeMessageHelper.setText("Dear " + nominatedUserName + ",<br>" +
                    "<br>" +
                    "We hope this message finds you well and filled with the same enthusiasm that you bring to our volunteer community every day." +
                    "<br>" +
                    "We wanted to take a moment to express our sincere gratitude for your recent nomination of a volunteer need through SERVE." +
                    "<br><br>" +
                    "Your nomination is a vital contribution to our efforts to better serve our community and address its needs effectively. We're eager to review your nomination." +
                    "<br><br>" +
                    "Thank you once again for your commitment and passion for serving others. We look forward to exploring your nomination further and keeping you updated on its progress." +
                    "<br>" +
                    "<br>" +
                    "Warm regards," +
                    "<br>" +
                    "Admin", true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


    @Async
    public CompletableFuture<Void> sendEmailToVolunteerAsync(String nominatedUserId, NominationStatus status, String needId) {
        try {
            sendEmailToVolunteer(nominatedUserId, status, needId);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return CompletableFuture.completedFuture(null);
    }

    public void sendEmailToVolunteer(String nominatedUserId, NominationStatus status, String needId) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        UserResponse userResponse = fetchUserResponse(nominatedUserId);
        String nominatedUserEmail = userResponse.getContactDetails().getEmail();
        String nominatedUserName = userResponse.getIdentityDetails().getFullname();

        NeedResponse needResponse = fetchNeedResponse(needId);
            String description = needResponse.getDescription();

        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
        mimeMessageHelper. setTo(nominatedUserEmail);
        try {
            if(status == NominationStatus.Approved) {
                mimeMessageHelper.setSubject("Confirmation: Your Volunteer Need Nomination");
                mimeMessageHelper.setText("Dear " + nominatedUserName + ",<br>" +
                        "<br>" +
                        "I hope this message finds you in good spirits." +
                        "<br>" +
                        "I'm delighted to share that your volunteer need nomination on the SERVE platform has been carefully reviewed and approved by our administrative team." +
                        "<br><br>" +
                        "Volunteer Need:" + description +
                        "<br><br>" +
                        "Your commitment to empowering rural children's education is deeply appreciated and highly valued." +
                        "<br>" +
                        "Please log in to the platform to access the Need Plan, where you'll find detailed information about the sessions and schedule"+
                        "<br><br>" +
                        "Wishing you all the best as you embark on these classes."+
                        "<br>"+
                        "<br>"+
                        "Warm regards," +
                        "<br>" +
                        "Admin", true);
                javaMailSender.send(mimeMessage);
            }
            else if(status == NominationStatus.Rejected) {
                mimeMessageHelper.setSubject("Update on Your Volunteer Need Nomination");
                mimeMessageHelper.setText("Dear " + nominatedUserName + ",<br>" +
                        "<br>" +
                        "I hope this email finds you well." +
                        "<br>" +
                        "I wanted to provide you with an update regarding your recent volunteer need nomination on the SERVE platform. After careful consideration, our administrative team has reviewed your suggestion, and unfortunately, we have decided not to proceed with the nomination at this time." +
                        "<br>" +
                        "While we deeply appreciate your initiative and dedication to making a positive impact, upon evaluation, we found that the nominated need may not align perfectly with our current timelines and required skill sets." +
                        "<br>" +
                        "Please understand that your commitment to serving others is immensely valued, and we encourage you to continue exploring opportunities that better match your availability and skills." +
                        "<br>" +
                        "If you have any questions or would like further clarification on our decision, please don't hesitate to reach out. We're here to support you in any way we can." +
                        "<br>" +
                        "Thank you for your understanding and ongoing support of our mission." +
                        "<br>" +
                        "<br>" +
                        "Warm regards," +
                        "<br>" +
                        "Admin", true);
                javaMailSender.send(mimeMessage);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}
