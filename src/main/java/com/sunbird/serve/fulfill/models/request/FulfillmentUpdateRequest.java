package com.sunbird.serve.fulfill.models.request;

import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Pattern;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class FulfillmentUpdateRequest {
   
   @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Need Plan ID must contain only alphanumeric characters and hyphens")
   private String needPlanId;
   
   @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Assigned User ID must contain only alphanumeric characters and hyphens")
   private String assignedUserId;
   
   @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Coordinator User ID must contain only alphanumeric characters and hyphens")
   private String coordUserId;
   
   private FulfillmentStatus fulfillmentStatus;
   
   @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Occurrence ID must contain only alphanumeric characters and hyphens")
   private String occurrenceId;
}
