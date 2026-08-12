package com.example.hackathon_team3_be.entity;

import com.example.hackathon_team3_be.dto.BagAttributes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "unseen_bags")
public class UnseenBagEntity {
    @Id private String unseenId;
    @Column(nullable = false) private String experienceId;
    private String imageUrl;
    private String category;
    private String shape;
    private String size;
    private String color;
    private String material;
    private String strap;
    private String detail;
    private String style;
    @Column(nullable = false) private Instant createdAt;

    protected UnseenBagEntity() { }

    public UnseenBagEntity(String unseenId, String experienceId, String imageUrl, BagAttributes a) {
        this.unseenId = unseenId; this.experienceId = experienceId; this.imageUrl = imageUrl;
        this.category = a.category(); this.shape = a.shape(); this.size = a.size();
        this.color = a.color(); this.material = a.material(); this.strap = a.strap();
        this.detail = a.detail(); this.style = a.style();
        this.createdAt = Instant.now();
    }

    public String getUnseenId() { return unseenId; }
    public String getExperienceId() { return experienceId; }
    public String getImageUrl() { return imageUrl; }
    public BagAttributes attributes() { return new BagAttributes(category, shape, size, color, material, strap, detail, style); }
}
