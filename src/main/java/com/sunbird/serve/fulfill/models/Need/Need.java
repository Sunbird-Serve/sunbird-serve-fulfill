package com.sunbird.serve.fulfill.models.Need;

import com.sunbird.serve.fulfill.models.enums.NeedStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for deserializing Need responses from the serve-need microservice.
 * This service does NOT own the Need table — all Need data is managed by serve-need.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Need {

    private UUID id;
    private String needTypeId;
    private String description;
    private String needPurpose;
    private String entityId;
    private String userId;
    private String requirementId;
    private String name;
    private NeedStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
