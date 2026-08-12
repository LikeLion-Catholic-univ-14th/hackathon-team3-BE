package com.example.hackathon_team3_be.dto;

import java.util.Set;

public record LockFeaturesResponse(String unseenId, Set<String> locked) { }
