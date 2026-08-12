package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.entity.PreferenceEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PreferenceRepository extends JpaRepository<PreferenceEntity, String> { }
