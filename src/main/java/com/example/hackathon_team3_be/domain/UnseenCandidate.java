package com.example.hackathon_team3_be.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "unseen_candidates",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_unseen_candidate_session_rank",
                columnNames = {"session_id", "display_rank"}
        )
)
public class UnseenCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private ExperienceSession session;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false, length = 50)
    private String shape;

    @Column(nullable = false, length = 30)
    private String size;

    @Column(nullable = false, length = 50)
    private String color;

    @Column(name = "display_rank", nullable = false)
    private int rank;

    @Column(nullable = false)
    private boolean selected;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
