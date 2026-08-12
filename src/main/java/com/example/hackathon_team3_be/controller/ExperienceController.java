package com.example.hackathon_team3_be.controller;

import com.example.hackathon_team3_be.dto.CreateExperienceRequest;
import com.example.hackathon_team3_be.dto.ExperienceResponse;
import com.example.hackathon_team3_be.service.ExperienceService;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/experiences")
public class ExperienceController {

    private final ExperienceService experienceService;

    public ExperienceController(ExperienceService experienceService) {
        this.experienceService = experienceService;
    }

    @PostMapping
    public ResponseEntity<ExperienceResponse> create(@Valid @RequestBody CreateExperienceRequest request) {
        ExperienceResponse response = experienceService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/experiences/" + response.experienceId()))
                .body(response);
    }
}
