package com.sunbird.serve.fulfill.models.request;

import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class FulfillmentRequest {
   private String needId;
   private String needPlanId;
   private String assignedUserId;
   private String coordUserId;
   private FulfillmentStatus fulfillmentStatus;
   private String occurrenceId;
}