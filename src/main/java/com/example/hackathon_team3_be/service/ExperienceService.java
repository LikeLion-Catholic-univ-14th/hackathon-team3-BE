package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.dto.CreateExperienceRequest;
import com.example.hackathon_team3_be.dto.ExperienceResponse;
import com.example.hackathon_team3_be.entity.ExperienceEntity;
import com.example.hackathon_team3_be.entity.ExperienceStatus;
import com.example.hackathon_team3_be.exception.DuplicateExperienceException;
import com.example.hackathon_team3_be.exception.ExperienceNotFoundException;
import com.example.hackathon_team3_be.repository.ExperienceRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ExperienceService {

    private final ExperienceRepository experienceRepository;

    public ExperienceService(ExperienceRepository experienceRepository) {
        this.experienceRepository = experienceRepository;
    }

    public ExperienceResponse create(CreateExperienceRequest request) {
        String experienceId = request != null && request.experienceId() != null && !request.experienceId().isBlank()
                ? request.experienceId() : "exp_" + UUID.randomUUID().toString().replace("-", "");
        if (experienceRepository.existsById(experienceId)) {
            throw new DuplicateExperienceException(experienceId);
        }

        Instant now = Instant.now();
        ExperienceEntity saved = experienceRepository.save(
                new ExperienceEntity(experienceId, "anon_" + UUID.randomUUID().toString().replace("-", ""),
                        request != null && request.status() != null ? request.status() : ExperienceStatus.COLLECTING_INPUT,
                        request != null && request.consentVersion() != null ? request.consentVersion() : "v1",
                        now, now.plus(Duration.ofDays(30)))
        );
        return toResponse(saved);
    }

    public ExperienceEntity getRequired(String experienceId) {
        return experienceRepository.findById(experienceId)
                .orElseThrow(() -> new ExperienceNotFoundException(experienceId));
    }

    static ExperienceResponse toResponse(ExperienceEntity experience) {
        return new ExperienceResponse(experience.getExperienceId(), experience.getStatus(),
                experience.getAnonymousCustomerId(), experience.getExpiresAt());
    }
}
