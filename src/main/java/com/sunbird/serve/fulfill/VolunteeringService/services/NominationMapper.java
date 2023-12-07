package com.sunbird.serve.fulfill;

import com.sunbird.serve.fulfill.models.request.NominationRequest;
import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import com.sunbird.serve.fulfill.models.Nomination.Nomination;
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

    // Add other mapping methods as needed
}
