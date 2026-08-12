package com.example.hackathon_team3_be.dto;

import jakarta.validation.constraints.NotBlank;

public record GenerateUnseenRequest(@NotBlank String experienceId, String category, String shape,
                                    String size, String favoriteColor, String material, String strap,
                                    String detail, String style) { }
