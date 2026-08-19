package com.example.hackathon_team3_be.domain;

import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.GenerationStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.InputMode;
import com.example.hackathon_team3_be.domain.DomainEnums.PurchaseResult;
import com.example.hackathon_team3_be.domain.DomainEnums.RevealStage;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "experience_sessions")
public class ExperienceSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String demoCustomerId;

    @Column(nullable = false, length = 80)
    private String customerName;

    @Column(length = 30)
    private String phone;

    @Column(length = 160)
    private String email;

    @Column(length = 30)
    private String gender;

    @Column(nullable = false)
    private boolean dataConsent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ExperienceStatus status = ExperienceStatus.CREATED;

    private String silhouette;
    private String structurePreference;
    private String proportion;
    private String color;
    private String attitude;

    @Column(length = 500)
    private String contexts;

    private String lockedAttribute;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private InputMode lastInputMode;

    @Column(length = 2000)
    private String freeTextInput;

    @Column(length = 2000)
    private String voiceTranscript;

    private String intentPurpose;
    private String intentPriority;
    private String intentStyle;
    private String intentSignature;
    private String intentConcern;

    @Column(length = 1000)
    private String intentSummary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private GenerationStatus unseenStatus = GenerationStatus.NOT_STARTED;

    @Column(unique = true, length = 30)
    private String unseenPublicId;

    @Column(length = 500)
    private String unseenImageUrl;

    @Column(length = 2000)
    private String unseenPrompt;

    @Column(length = 1000)
    private String unseenError;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UnseenCandidate> unseenCandidates = new ArrayList<>();

    private String advisorPriority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private RevealStage revealStage = RevealStage.NOT_STARTED;

    @Column(length = 500)
    private String feedbackLoved;

    @Column(length = 500)
    private String feedbackConcern;

    @Column(length = 500)
    private String feedbackWants;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PurchaseResult purchaseResult;

    private Long purchasedProductId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
