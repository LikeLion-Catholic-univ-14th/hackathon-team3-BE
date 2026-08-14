package com.example.hackathon_team3_be.repository;

import com.example.hackathon_team3_be.domain.AdvisorEdit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdvisorEditRepository extends JpaRepository<AdvisorEdit, UUID> {
    List<AdvisorEdit> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    void deleteBySessionId(UUID sessionId);
}
