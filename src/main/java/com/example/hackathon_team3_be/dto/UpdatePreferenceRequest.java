package com.example.hackathon_team3_be.dto;

import java.util.Set;

public record UpdatePreferenceRequest(
        String bagType, String size, Set<String> colors, Set<String> materials,
        String metalTone, Set<String> usage, Set<String> moodKeywords, String freeText
) { }
