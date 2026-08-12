package com.example.hackathon_team3_be.controller;

import com.example.hackathon_team3_be.dto.UpdatePreferenceRequest;
import com.example.hackathon_team3_be.service.PreferenceService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/experiences/{experienceId}/preferences")
public class PreferenceController {
    private final PreferenceService preferenceService;

    public PreferenceController(PreferenceService preferenceService) { this.preferenceService = preferenceService; }

    @PutMapping
    public ResponseEntity<Void> update(@PathVariable String experienceId,
                                       @RequestBody UpdatePreferenceRequest request) {
        preferenceService.save(experienceId, request);
        return ResponseEntity.noContent().build();
    }
}
