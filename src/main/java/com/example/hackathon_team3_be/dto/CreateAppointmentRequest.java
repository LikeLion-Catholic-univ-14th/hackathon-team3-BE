package com.example.hackathon_team3_be.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record CreateAppointmentRequest(
        @NotBlank String experienceId,@NotBlank String unseenId,@NotBlank String storeId,
        @NotNull OffsetDateTime startAt,@NotBlank String customerName,
        @NotBlank String phone,@Email @NotBlank String email,
        @AssertTrue boolean privacyAgreed) { }
