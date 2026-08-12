package com.example.hackathon_team3_be.entity;

import com.example.hackathon_team3_be.dto.UpdatePreferenceRequest;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "preferences")
public class PreferenceEntity {
    @Id
    @Column(name = "experience_id")
    private String experienceId;
    private String bagType;
    private String size;
    private String metalTone;
    @Column(length = 2000)
    private String freeText;

    @ElementCollection
    @CollectionTable(name = "preference_colors", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "color")
    private Set<String> colors = new LinkedHashSet<>();
    @ElementCollection
    @CollectionTable(name = "preference_materials", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "material")
    private Set<String> materials = new LinkedHashSet<>();
    @ElementCollection
    @CollectionTable(name = "preference_usage", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "usage")
    private Set<String> usage = new LinkedHashSet<>();
    @ElementCollection
    @CollectionTable(name = "preference_moods", joinColumns = @JoinColumn(name = "experience_id"))
    @Column(name = "mood_keyword")
    private Set<String> moodKeywords = new LinkedHashSet<>();

    protected PreferenceEntity() { }

    public PreferenceEntity(String experienceId, UpdatePreferenceRequest request) {
        this.experienceId = experienceId;
        this.bagType = request.bagType();
        this.size = request.size();
        this.colors = copy(request.colors());
        this.materials = copy(request.materials());
        this.metalTone = request.metalTone();
        this.usage = copy(request.usage());
        this.moodKeywords = copy(request.moodKeywords());
        this.freeText = request.freeText();
    }

    private static Set<String> copy(Set<String> values) {
        return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values);
    }
}
