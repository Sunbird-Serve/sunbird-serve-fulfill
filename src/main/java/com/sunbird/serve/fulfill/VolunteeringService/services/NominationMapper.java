package com.sunbird.serve.fulfill;

import com.sunbird.serve.fulfill.models.request.NominationRequest;
import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import com.sunbird.serve.fulfill.models.Nomination.Nomination;
import com.sunbird.serve.fulfill.models.request.FulfillmentRequest;
import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import com.sunbird.serve.fulfill.models.Nomination.Fulfillment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

public class NominationMapper {

    public static Nomination mapToEntity(NominationRequest nominationRequest) {
        Nomination nomination = new Nomination();
        nomination.setNeedId(nominationRequest.getNeedId());
        nomination.setNominatedUserId(nominationRequest.getNominatedUserId());
        nomination.setComments(nominationRequest.getComments());
        nomination.setNominationStatus(NominationStatus.Nominated); // Set default status or customize as needed
        // Set any other fields...

        return nomination;
    }

    public static Fulfillment mapToEntityFulfill(FulfillmentRequest fulfillmentRequest) {
        Fulfillment fulfillment = new Fulfillment();
        fulfillment.setNeedId(fulfillmentRequest.getNeedId());
        fulfillment.setNeedPlanId(fulfillmentRequest.getNeedPlanId());
        fulfillment.setAssignedUserId(fulfillmentRequest.getAssignedUserId());
        fulfillment.setCoordUserId(fulfillmentRequest.getCoordUserId());
        fulfillment.setFulfillmentStatus(FulfillmentStatus.InProgress); // Set default status or customize as needed
        // Set any other fields...

        return fulfillment;
    }

    // Add other mapping methods as needed
}
