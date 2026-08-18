package com.example.hackathon_team3_be.api;

import com.example.hackathon_team3_be.domain.DomainEnums.EditDirection;
import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.GenerationStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.InputMode;
import com.example.hackathon_team3_be.domain.DomainEnums.InputStep;
import com.example.hackathon_team3_be.domain.DomainEnums.PurchaseResult;
import com.example.hackathon_team3_be.domain.DomainEnums.ReservationStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.RevealStage;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record CreateSessionRequest(
            @NotBlank @Size(max = 80) String demoCustomerId,
            @NotBlank @Size(max = 80) String customerName,
            boolean dataConsent
    ) {
    }

    public record PreferenceRequest(
            @NotBlank String silhouette,
            @NotBlank String structure,
            @NotBlank String proportion,
            @NotBlank String color,
            @NotBlank String attitude,
            @NotEmpty List<@NotBlank String> contexts,
            @NotBlank String lockedAttribute
    ) {
    }

    public record SessionResponse(
            UUID sessionId,
            String demoCustomerId,
            String customerName,
            boolean dataConsent,
            ExperienceStatus status,
            PreferenceResponse preferences,
            IntentProfileResponse intentProfile,
            UnseenResponse unseen,
            String advisorPriority,
            RevealStage revealStage,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record PreferenceResponse(
            String silhouette,
            String structure,
            String proportion,
            String color,
            String attitude,
            List<String> contexts,
            String lockedAttribute
    ) {
    }

    public record ChoiceOptionResponse(String value, String label) {
    }

    public record ChoiceStepResponse(
            InputStep step,
            String title,
            String prompt,
            boolean multiple,
            List<ChoiceOptionResponse> options
    ) {
    }

    public record PreferenceCatalogResponse(List<ChoiceStepResponse> steps) {
    }

    public record ChoiceInputRequest(
            @NotNull InputStep step,
            @NotEmpty @Size(max = 4) List<@NotBlank String> values
    ) {
    }

    public record TextInputRequest(@NotBlank @Size(max = 2000) String text) {
    }

    public record ContinuePreferenceRequest(UUID sourceSessionId) {
    }

    public record InputProgressResponse(
            int completedSteps,
            int totalSteps,
            int percent,
            InputStep nextStep,
            String nextPrompt,
            boolean readyForIntent,
            int recommendedTransitionDelayMs,
            String paceMessage,
            InputMode lastInputMode,
            PreferenceResponse preferences
    ) {
    }

    public record InputInterpretationResponse(
            InputMode inputMode,
            String transcript,
            String transcriptionSource,
            Map<String, List<String>> extracted,
            List<String> appliedFields,
            InputProgressResponse progress
    ) {
    }

    public record IntentProfileResponse(
            String purpose,
            String priority,
            String style,
            String signature,
            String lockedAttribute,
            String concern,
            String summary
    ) {
    }

    public record UnseenResponse(
            String unseenId,
            GenerationStatus status,
            String imageUrl,
            String error
    ) {
    }

    public record StoreResponse(
            Long id, String name, String city, String address,
            Double latitude, Double longitude, Double distanceKm
    ) {
    }

    public record SlotResponse(LocalDate date, LocalDateTime scheduledAt, boolean available) {
    }

    public record ReservationRequest(
            @NotNull UUID sessionId,
            @NotNull Long storeId,
            @NotNull @Future LocalDateTime scheduledAt
    ) {
    }

    public record ReservationUpdateRequest(
            @NotNull Long storeId,
            @NotNull @Future LocalDateTime scheduledAt
    ) {
    }

    public record ReservationResponse(
            UUID reservationId,
            UUID sessionId,
            Long storeId,
            String storeName,
            LocalDateTime scheduledAt,
            ReservationStatus status,
            String passCode
    ) {
    }

    public record AppointmentResponse(
            ReservationResponse reservation,
            String customerName,
            String unseenId,
            String intentSummary
    ) {
    }

    public record IntentCardResponse(
            UUID sessionId,
            String customerName,
            String unseenId,
            String unseenImageUrl,
            IntentProfileResponse intentProfile,
            String advisorPriority,
            ReservationResponse reservation
    ) {
    }

    public record AdvisorTouchRequest(@NotBlank @Size(max = 100) String priority) {
    }

    public record AdvisorEditItem(
            UUID editId,
            EditDirection direction,
            Long productId,
            String sku,
            String productName,
            String productImageUrl,
            String strap,
            String accessory,
            String rationale
    ) {
    }

    public record PersonalEditResponse(UUID sessionId, List<AdvisorEditItem> edits) {
    }

    public record PassRecognitionResponse(
            boolean recognized,
            UUID sessionId,
            String customerName,
            String unseenId,
            ReservationResponse reservation
    ) {
    }

    public record RevealResponse(UUID sessionId, RevealStage stage, ExperienceStatus sessionStatus) {
    }

    public record FeedbackRequest(
            List<@NotBlank String> loved,
            List<@NotBlank String> concerns,
            List<@NotBlank String> wants,
            @NotNull PurchaseResult result,
            Long purchasedProductId
    ) {
    }

    public record FeedbackResponse(UUID sessionId, PurchaseResult result, ExperienceStatus status) {
    }

    public record MemoryItem(
            UUID sessionId,
            String unseenId,
            String intentSummary,
            List<String> loved,
            List<String> concerns,
            List<String> wants,
            PurchaseResult result,
            Instant experiencedAt
    ) {
    }

    public record CustomerMemoryResponse(String demoCustomerId, List<MemoryItem> experiences) {
    }
}
