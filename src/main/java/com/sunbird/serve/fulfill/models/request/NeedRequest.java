package com.sunbird.serve.fulfill.models.request;

import com.sunbird.serve.fulfill.models.enums.NeedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeedRequest {

   private String needTypeId;
   private String name;
   private String needPurpose;
   private String description;
   private NeedStatus status;
   private String userId;
   private String entityId;
   private String requirementId;
}