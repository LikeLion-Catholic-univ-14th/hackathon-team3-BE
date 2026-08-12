package com.example.hackathon_team3_be.exception;

public class ExperienceNotFoundException extends RuntimeException {
    public ExperienceNotFoundException(String experienceId) {
        super("Experience not found: " + experienceId);
    }
}
