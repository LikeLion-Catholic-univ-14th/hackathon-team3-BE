package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.api.ApiDtos.AdvisorEditItem;
import com.example.hackathon_team3_be.api.ApiDtos.IntentProfileResponse;
import com.example.hackathon_team3_be.api.ApiDtos.PreferenceResponse;
import com.example.hackathon_team3_be.api.ApiDtos.ReservationResponse;
import com.example.hackathon_team3_be.api.ApiDtos.SessionResponse;
import com.example.hackathon_team3_be.api.ApiDtos.UnseenResponse;
import com.example.hackathon_team3_be.domain.AdvisorEdit;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.domain.Reservation;

import java.util.Arrays;
import java.util.List;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static SessionResponse toSession(ExperienceSession session) {
        return new SessionResponse(
                session.getId(),
                session.getDemoCustomerId(),
                session.getCustomerName(),
                session.getPhone(),
                session.getEmail(),
                session.getGender(),
                session.isDataConsent(),
                session.getStatus(),
                toPreference(session),
                toIntent(session),
                toUnseen(session),
                session.getAdvisorPriority(),
                session.getRevealStage(),
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }

    public static PreferenceResponse toPreference(ExperienceSession session) {
        if (session.getSilhouette() == null
                && session.getStructurePreference() == null
                && session.getProportion() == null
                && session.getColor() == null
                && session.getAttitude() == null
                && session.getContexts() == null
                && session.getLockedAttribute() == null) {
            return null;
        }
        return new PreferenceResponse(
                session.getSilhouette(),
                session.getStructurePreference(),
                session.getProportion(),
                session.getColor(),
                session.getAttitude(),
                split(session.getContexts()),
                session.getLockedAttribute()
        );
    }

    public static IntentProfileResponse toIntent(ExperienceSession session) {
        if (session.getIntentPurpose() == null) {
            return null;
        }
        return new IntentProfileResponse(
                session.getIntentPurpose(),
                session.getIntentPriority(),
                session.getIntentStyle(),
                session.getIntentSignature(),
                session.getLockedAttribute(),
                session.getIntentConcern(),
                session.getIntentSummary()
        );
    }

    public static UnseenResponse toUnseen(ExperienceSession session) {
        return new UnseenResponse(
                session.getUnseenPublicId(),
                session.getUnseenStatus(),
                session.getUnseenImageUrl(),
                session.getUnseenError()
        );
    }

    public static ReservationResponse toReservation(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getSession().getId(),
                reservation.getStore().getId(),
                reservation.getStore().getName(),
                reservation.getScheduledAt(),
                reservation.getStatus(),
                reservation.getPassCode()
        );
    }

    public static AdvisorEditItem toEdit(AdvisorEdit edit) {
        return new AdvisorEditItem(
                edit.getId(),
                edit.getDirection(),
                edit.getProduct().getId(),
                edit.getProduct().getSku(),
                edit.getProduct().getName(),
                edit.getProduct().getImageUrl(),
                edit.getStrap(),
                edit.getAccessory(),
                edit.getRationale()
        );
    }

    public static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .filter(item -> !item.isBlank())
                .toList();
    }
}
