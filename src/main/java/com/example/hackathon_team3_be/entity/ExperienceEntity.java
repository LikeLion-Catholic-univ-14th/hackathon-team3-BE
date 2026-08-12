package com.example.hackathon_team3_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "experiences")
public class ExperienceEntity {

    @Id
    @Column(name = "experience_id", nullable = false, updatable = false, length = 100)
    private String experienceId;

    @Column(name = "anonymous_customer_id", nullable = false, updatable = false, length = 50)
    private String anonymousCustomerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ExperienceStatus status;

    @Column(name = "consent_version", nullable = false, length = 30)
    private String consentVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    protected ExperienceEntity() {
    }

    public ExperienceEntity(String experienceId, String anonymousCustomerId, ExperienceStatus status,
                            String consentVersion, Instant createdAt, Instant expiresAt) {
        this.experienceId = experienceId;
        this.anonymousCustomerId = anonymousCustomerId;
        this.status = status;
        this.consentVersion = consentVersion;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getExperienceId() {
        return experienceId;
    }

    public ExperienceStatus getStatus() {
        return status;
    }

    public String getAnonymousCustomerId() { return anonymousCustomerId; }

    public Instant getExpiresAt() { return expiresAt; }
}
