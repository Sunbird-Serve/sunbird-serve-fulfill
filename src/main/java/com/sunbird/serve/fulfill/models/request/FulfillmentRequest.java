package com.sunbird.serve.fulfill.models.request;

import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Component
public class FulfillmentRequest {
   
   @NotBlank(message = "Need ID is required")
   @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Need ID must contain only alphanumeric characters and hyphens")
   private String needId;
   
   @NotBlank(message = "Need Plan ID is required")
   @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Need Plan ID must contain only alphanumeric characters and hyphens")
   private String needPlanId;
   
   @NotBlank(message = "Assigned User ID is required")
   @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Assigned User ID must contain only alphanumeric characters and hyphens")
   private String assignedUserId;
   
   @NotBlank(message = "Coordinator User ID is required")
   @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Coordinator User ID must contain only alphanumeric characters and hyphens")
   private String coordUserId;
   
   @NotNull(message = "Fulfillment status is required")
   private FulfillmentStatus fulfillmentStatus;
   
   @Pattern(regexp = "^[a-zA-Z0-9-]*$", message = "Occurrence ID must contain only alphanumeric characters and hyphens")
   private String occurrenceId;
}