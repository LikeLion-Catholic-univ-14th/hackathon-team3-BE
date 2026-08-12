package com.example.hackathon_team3_be.dto;

import java.util.List;

public record MatchResponse(String unseenId, List<ProductMatch> products) { }
