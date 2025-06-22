package com.sunbird.serve.fulfill.VolunteeringService.services;

import com.sunbird.serve.fulfill.FulfillmentRepository;
import com.sunbird.serve.fulfill.NominationMapper;
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
import com.sunbird.serve.fulfill.models.needresponse.NeedResponse;
import com.sunbird.serve.fulfill.models.userresponse.UserResponse;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import java.util.concurrent.Future;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;


@Service
public class FulfillmentService {

    private static final Logger logger = LoggerFactory.getLogger(FulfillmentService.class);

    private final FulfillmentRepository fulfillmentRepository;
    private final WebClient webClient;
    private final FulfillmentRequest fulfillmentRequest;

     @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private EmailTemplateService emailTemplateService;

    private final RestTemplate restTemplate;

    @Value("${serve.need.url}")
    private String serveNeedUrl;

    @Value("${serve.volunteering.url}")
    private String serveVolunteeringUserUrl;

    @Autowired
    public FulfillmentService(FulfillmentRepository fulfillmentRepository, 
    WebClient.Builder webClientBuilder, FulfillmentRequest fulfillmentRequest, RestTemplate restTemplate,
    @Value("${serve.url}") String serveUrl) {
        this.fulfillmentRepository = fulfillmentRepository;
        this.webClient = webClientBuilder.baseUrl(serveUrl).build();
        this.fulfillmentRequest = fulfillmentRequest;
        this.restTemplate = restTemplate;
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

    private NeedResponse fetchNeedResponse(String needId) {
        String serveNeedApi = serveNeedUrl + "/api/v1/serve-need/need/" + needId;
        ResponseEntity<NeedResponse> responseEntity = restTemplate.getForEntity(serveNeedApi, NeedResponse.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new HttpStatusCodeException(responseEntity.getStatusCode(), "Failed to fetch need response") {};
        }
        return responseEntity.getBody();
    }

    private UserResponse fetchUserResponse(String userId) {
        String serveVolunteeringApi = serveVolunteeringUserUrl + "/api/v1/serve-volunteering/user/" + userId;
        ResponseEntity<UserResponse> responseEntity = restTemplate.getForEntity(serveVolunteeringApi, UserResponse.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new HttpStatusCodeException(responseEntity.getStatusCode(), "Failed to fetch user response") {};
        }
        return responseEntity.getBody();
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

    public Fulfillment getFulfillmentForNeed(String needId, Map<String, String> headers) {
        return fulfillmentRepository.findAllByNeedId(needId);
    }

    @Async
    public CompletableFuture<Void> sendSessionCancelEmailAsync(String needId, Map<String, Object> deliverableDetails){
        try {
            sendSessionCancelEmail(needId, deliverableDetails);
        } catch (Exception e) {
            logger.error("Failed to send session cancel email for needId: {}", needId, e);
        }
        return CompletableFuture.completedFuture(null);
    }

    public void sendSessionCancelEmail(String needId, Map<String, Object> deliverableDetails) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            Fulfillment fulfillment = fulfillmentRepository.findAllByNeedId(needId);

            UserResponse userResponse = fetchUserResponse(fulfillment.getCoordUserId());
            String coordUserEmail = userResponse.getContactDetails().getEmail();
            String coordUserName = userResponse.getIdentityDetails().getFullname();

            String needPlanId = (String) deliverableDetails.get("needPlanId");
            String comments = (String) deliverableDetails.get("comments");
            String status = (String) deliverableDetails.get("status");
            String deliverableDate = (String) deliverableDetails.get("deliverableDate");

            userResponse = fetchUserResponse(fulfillment.getAssignedUserId());
            String assignedUserEmail = userResponse.getContactDetails().getEmail();
            String assignedUserName = userResponse.getIdentityDetails().getFullname();

            NeedResponse needResponse = fetchNeedResponse(needId);
            String needName = needResponse.getName();
            String description = needResponse.getDescription();

            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            String subject = emailTemplateService.getCancelSessionEmailSubject(deliverableDate);
            String body = emailTemplateService.getCancelSessionEmailBody(coordUserName, deliverableDate, comments,needName, assignedUserName, fulfillment.getAssignedUserId());

            mimeMessageHelper.setTo(coordUserEmail);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body, true);
            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            logger.error("Failed to send session cancel email for needId: {}", needId, e);
        }
    }

}
