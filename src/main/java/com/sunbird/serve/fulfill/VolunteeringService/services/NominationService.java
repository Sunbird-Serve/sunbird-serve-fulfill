package com.sunbird.serve.fulfill;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.sunbird.serve.fulfill.models.Nomination.Nomination;
import com.sunbird.serve.fulfill.models.enums.NominationStatus;
import com.sunbird.serve.fulfill.models.request.NominationRequest;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;


@Service
public class NominationService {

    private final NominationRepository nominationRepository;

    @Autowired
    public NominationService(NominationRepository nominationRepository) {
        this.nominationRepository = nominationRepository;
    }

    public Nomination nominateNeed(NominationRequest nominationRequest) {
        // Convert NominationRequest to Nomination entity
        Nomination nomination = NominationMapper.mapToEntity(nominationRequest);

        // You can perform any additional business logic/validation here before saving

        // Save the entity
        return nominationRepository.save(nomination);
    }

    public List<Nomination> getAllNominations(String needId, Map<String, String> headers) {
        return nominationRepository.findAllByNeedId(needId);
    }

    public List<Nomination> getAllNominationsByStatus(String needId, NominationStatus status, Map<String, String> headers) {
        return nominationRepository.findAllByNeedIdAndNominationStatus(needId, status);
    }


    public List<Nomination> getAllNominationForUser(String nominatedUserId, Integer page, Integer size, Map<String, String> headers) {
        return nominationRepository.findAllByNominatedUserId(nominatedUserId);
    }

    // Add other service methods as needed
}
