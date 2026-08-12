package com.example.hackathon_team3_be.dto;

import java.math.BigDecimal;

public record ProductMatch(String productId, String name, String imageUrl,
                           BigDecimal price, int matchScore) { }
