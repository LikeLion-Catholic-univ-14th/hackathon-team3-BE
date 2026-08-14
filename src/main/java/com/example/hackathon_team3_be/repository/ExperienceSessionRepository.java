package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.domain.ExperienceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExperienceSessionRepository extends JpaRepository<ExperienceSession, UUID> {
    Optional<ExperienceSession> findByUnseenPublicId(String unseenPublicId);
    List<ExperienceSession> findByDemoCustomerIdOrderByCreatedAtDesc(String demoCustomerId);
}
