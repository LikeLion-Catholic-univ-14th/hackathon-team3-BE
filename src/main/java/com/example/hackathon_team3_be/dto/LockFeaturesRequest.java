package com.example.hackathon_team3_be.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public record LockFeaturesRequest(@NotEmpty Set<String> features) { }
