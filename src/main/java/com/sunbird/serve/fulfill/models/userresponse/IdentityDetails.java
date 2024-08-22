package com.sunbird.serve.fulfill.models.userresponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdentityDetails { 

   private String osUpdatedAt;
   private String gender;
   private String osCreatedAt;
   private String osUpdatedBy;
   private String dob;
   private String name;
   private String osCreatedBy;
   private String osid;
   private String fullname;

   @JsonProperty("Nationality")
   private String Nationality;

}