package com.sunbird.serve.fulfill.models.request;

import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NominationRequest {

    @NotBlank(message = "Need ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Need ID must contain only alphanumeric characters and hyphens")
    private String needId;
    
    @NotBlank(message = "Nominated User ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-]+$", message = "Nominated User ID must contain only alphanumeric characters and hyphens")
    private String nominatedUserId;
    
    @Size(max = 1000, message = "Comments must not exceed 1000 characters")
    private String comments;
    
    @NotNull(message = "Nomination status is required")
    private NominationStatus status;
}
