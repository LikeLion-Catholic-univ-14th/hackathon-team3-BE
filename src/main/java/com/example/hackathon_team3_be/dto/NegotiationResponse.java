package com.example.hackathon_team3_be.dto;

import java.util.Map;
import java.util.Set;

public record NegotiationResponse(String unseenId, Set<String> locked,
                                  ProductMatch recommendedProduct,
                                  Map<String, FeatureChange> changedFeatures) { }
