package com.example.hackathon_team3_be.dto;

import com.example.hackathon_team3_be.entity.ExperienceStatus;
import java.time.Instant;

public record ExperienceResponse(String experienceId, ExperienceStatus status,
                                 String anonymousCustomerId, Instant expiresAt) {
}
