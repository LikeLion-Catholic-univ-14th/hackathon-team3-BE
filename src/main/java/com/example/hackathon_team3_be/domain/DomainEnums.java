package com.example.hackathon_team3_be.domain;

public final class DomainEnums {

    private DomainEnums() {
    }

    public enum ExperienceStatus {
        CREATED,
        PREFERENCES_SAVED,
        INTENT_READY,
        UNSEEN_PROCESSING,
        UNSEEN_READY,
        RESERVED,
        ARRIVED,
        PERSONAL_EDIT_READY,
        REVEALING,
        COMPLETED
    }

    public enum GenerationStatus {
        NOT_STARTED,
        PROCESSING,
        READY,
        FAILED
    }

    public enum ReservationStatus {
        BOOKED,
        ARRIVED,
        COMPLETED,
        CANCELLED
    }

    public enum RevealStage {
        NOT_STARTED,
        WELCOME,
        UNSEEN_REVEAL,
        LIFESTYLE_SCENE,
        FINAL_TRANSITION,
        COMPLETED
    }

    public enum PurchaseResult {
        PURCHASED,
        NOT_PURCHASED,
        UNDECIDED
    }

    public enum EditDirection {
        THE_EVERYDAY,
        THE_NOMAD,
        THE_UNEXPECTED
    }

    public enum InputMode {
        CHOICE,
        TEXT,
        VOICE,
        PREVIOUS_SESSION
    }

    public enum InputStep {
        SILHOUETTE,
        STRUCTURE,
        PROPORTION,
        COLOR,
        ATTITUDE,
        CONTEXTS,
        LOCKED_ATTRIBUTE
    }
}
