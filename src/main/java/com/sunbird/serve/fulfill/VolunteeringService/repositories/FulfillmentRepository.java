package com.sunbird.serve.fulfill;

import com.sunbird.serve.fulfill.models.Nomination.Fulfillment;
import com.sunbird.serve.fulfill.models.enums.FulfillmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface FulfillmentRepository extends JpaRepository<Fulfillment, UUID> {
    List<Fulfillment> findAllByAssignedUserId(String assignedUserId);

    List<Fulfillment> findAllByCoordUserId(String coordUserId);
}
