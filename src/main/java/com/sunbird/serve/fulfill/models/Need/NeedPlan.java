package com.sunbird.serve.fulfill.models.Need;

import com.sunbird.serve.fulfill.models.enums.NeedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for deserializing NeedPlan responses from the serve-need microservice.
 * This service does NOT own the NeedPlan table — all NeedPlan data is managed by serve-need.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeedPlan {

    private UUID id;
    private String needId;
    private String name;
    private NeedStatus status;
    private String occurrenceId;
    private Instant createdAt;
    private Instant updatedAt;
}
