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
public class UserStatusRequest {

   private String status;
   private Boolean send;
}