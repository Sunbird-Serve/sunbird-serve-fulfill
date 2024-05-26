package com.sunbird.serve.fulfill.VolunteeringService.controllers;

import com.sunbird.serve.fulfill.VolunteeringService.services.FulfillmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.sunbird.serve.fulfill.models.Nomination.Fulfillment;
import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import com.sunbird.serve.fulfill.models.request.FulfillmentRequest;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;


@RestController
@CrossOrigin(origins = "*")
public class FulfillmentController {

    private final FulfillmentService fulfillmentService;

    @Autowired
    public FulfillmentController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    //Nominate a need
    @Operation(summary = "Fulfilment Details Created", description = "Create Fulfillment Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Created Fulfillment Details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Bad Input"),
            @ApiResponse(responseCode = "500", description = "Server Error")}
    )
    @PostMapping("/fulfillment/{needId}")
    public ResponseEntity<Fulfillment> createFulfillment( @PathVariable String needId,
            @RequestBody FulfillmentRequest request,
            @RequestHeader Map<String, String> headers) {

        Fulfillment response = fulfillmentService.createFulfillment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //Fetch Fulfillment for Assigned UserResponse Id
    @Operation(summary = "Fulfilment Details For Assigned UserResponse Id", description = "Fetch Fulfillment Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Fetched Fulfillment Details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Bad Input"),
            @ApiResponse(responseCode = "500", description = "Server Error")}
    )
    @GetMapping("/fulfillment/volunteer-read/{assignedUserId}")
    public ResponseEntity<List<Fulfillment>> getFulfillmentForAssignedUser(
            @PathVariable String assignedUserId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader Map<String, String> headers) {
        List<Fulfillment> fulfillment = fulfillmentService.getFulfillmentForAssignedUser(assignedUserId, page, size, headers);
        return ResponseEntity.ok(fulfillment);
    }

    //Fetch Fulfillment for Need Coordinator
    @Operation(summary = "Fulfilment Details For Need Coordinator", description = "Fetch Fulfillment Details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully Fetched Fulfillment Details", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "400", description = "Bad Input"),
            @ApiResponse(responseCode = "500", description = "Server Error")}
    )
    @GetMapping("/fulfillment/coordinator-read/{coordUserId}")
    public ResponseEntity<List<Fulfillment>> getFulfillmentForCoordUser(
            @PathVariable String coordUserId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader Map<String, String> headers) {
        List<Fulfillment> fulfillment = fulfillmentService.getFulfillmentForCoordUser(coordUserId, page, size, headers);
        return ResponseEntity.ok(fulfillment);
    }
}
