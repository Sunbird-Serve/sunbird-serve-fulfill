package com.sunbird.serve.fulfill.models.Need;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for deserializing Occurrence responses from the serve-need microservice.
 * This service does NOT own the Occurrence table — all Occurrence data is managed by serve-need.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Occurrence {

    private UUID id;
    private Instant startDate;
    private Instant endDate;
    private String days;
    private String frequency;
    private Instant createdAt;
    private Instant updatedAt;
}
