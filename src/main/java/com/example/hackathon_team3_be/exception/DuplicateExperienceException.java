package com.example.hackathon_team3_be.exception;

public class DuplicateExperienceException extends RuntimeException {

    public DuplicateExperienceException(String experienceId) {
        super("Experience already exists: " + experienceId);
    }
}
