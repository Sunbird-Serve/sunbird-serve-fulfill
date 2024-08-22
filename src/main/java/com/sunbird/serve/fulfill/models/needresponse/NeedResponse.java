package com.sunbird.serve.fulfill.models.needresponse;

import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NeedResponse {
	private String needPurpose;
	private String createdAt;
	private String needTypeId;
	private String name;
	private String description;
	private String entityId;
	private String id;
	private String requirementId;
	private String userId;
	private String status;
	private String updatedAt;
}