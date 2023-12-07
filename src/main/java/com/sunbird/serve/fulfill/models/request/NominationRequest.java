package com.sunbird.serve.fulfill.models.request;

import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NominationRequest {

    private String needId;
    private String nominatedUserId;
    private String comments;
    private NominationStatus status;
}
