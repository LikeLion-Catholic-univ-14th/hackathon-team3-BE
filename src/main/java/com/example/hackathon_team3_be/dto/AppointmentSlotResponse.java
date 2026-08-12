package com.example.hackathon_team3_be.dto;

import java.time.OffsetDateTime;

public record AppointmentSlotResponse(OffsetDateTime startAt,boolean available) { }
