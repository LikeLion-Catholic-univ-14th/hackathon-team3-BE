package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.dto.UpdatePreferenceRequest;
import com.example.hackathon_team3_be.entity.PreferenceEntity;
import com.example.hackathon_team3_be.repository.PreferenceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PreferenceService {
    private final ExperienceService experienceService;
    private final PreferenceRepository preferenceRepository;

    public PreferenceService(ExperienceService experienceService, PreferenceRepository preferenceRepository) {
        this.experienceService = experienceService;
        this.preferenceRepository = preferenceRepository;
    }

    @Transactional
    public void save(String experienceId, UpdatePreferenceRequest request) {
        experienceService.getRequired(experienceId);
        preferenceRepository.save(new PreferenceEntity(experienceId, request));
    }
}
