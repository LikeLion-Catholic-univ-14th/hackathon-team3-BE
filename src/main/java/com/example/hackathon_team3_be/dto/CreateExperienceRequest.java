package com.example.hackathon_team3_be.dto;

import com.example.hackathon_team3_be.entity.ExperienceStatus;
import jakarta.validation.constraints.NotBlank;

public record CreateExperienceRequest(
        @NotBlank String experienceId,
        ExperienceStatus status,
        String consentVersion
) {
}
