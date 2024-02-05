package com.sunbird.serve.fulfill;

import com.sunbird.serve.fulfill.models.Nomination.Nomination;
import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface NominationRepository extends JpaRepository<Nomination, UUID> {

    List<Nomination> findAllByNeedId(String needId);

    List<Nomination> findAllByNeedIdAndNominationStatus(String needId, NominationStatus nominationStatus);

    List<Nomination> findAllByNominatedUserId(String nominatedUserId);

}
