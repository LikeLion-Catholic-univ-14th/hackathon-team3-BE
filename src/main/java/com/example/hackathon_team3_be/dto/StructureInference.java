package com.example.hackathon_team3_be.dto;

import java.util.List;

public record StructureInference(
        String value,
        String source,
        double confidence,
        List<String> reasons
) { }
