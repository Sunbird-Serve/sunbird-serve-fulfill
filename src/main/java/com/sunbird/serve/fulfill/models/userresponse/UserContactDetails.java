package com.sunbird.serve.fulfill.models.userresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserContactDetails {

    private String osUpdatedAt;
    private UserAddress address;
    private String osCreatedAt;
    private String osUpdatedBy;
    private String mobile;
    private String osCreatedBy;
    private String osid;
    private String email;

}
