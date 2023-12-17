package com.sunbird.serve.fulfill.models.request;

import com.sunbird.serve.fulfill.models.enums.NeedStatus;
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
public class NeedPlanRequest {
   private String assignedUserId;
   private String needId;
   private String name;
   private NeedStatus status;
   private String occurrenceId;
}