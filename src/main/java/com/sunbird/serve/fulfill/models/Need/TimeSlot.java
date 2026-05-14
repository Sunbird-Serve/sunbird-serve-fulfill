package com.sunbird.serve.fulfill.models.Need;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for deserializing TimeSlot responses from the serve-need microservice.
 * This service does NOT own the TimeSlot table — all TimeSlot data is managed by serve-need.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {

    private UUID id;
    private String occurrenceId;
    private Instant startTime;
    private Instant endTime;
    private String day;
    private Instant createdAt;
    private Instant updatedAt;
}
