package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.entity.ExperienceEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExperienceRepository extends JpaRepository<ExperienceEntity, String> {
}
