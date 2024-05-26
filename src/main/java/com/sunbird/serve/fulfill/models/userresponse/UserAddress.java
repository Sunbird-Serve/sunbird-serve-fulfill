package com.sunbird.serve.fulfill.models.userresponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {

    private String osUpdatedAt;
    private String osCreatedAt;
    private String osUpdatedBy;
    private String osCreatedBy;
    private String osid;
    private String state;
    private String city;
    private String country;

}
