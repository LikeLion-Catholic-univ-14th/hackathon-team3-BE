package com.example.hackathon_team3_be.api;

import com.example.hackathon_team3_be.api.ApiDtos.AdvisorTouchRequest;
import com.example.hackathon_team3_be.api.ApiDtos.AppointmentResponse;
import com.example.hackathon_team3_be.api.ApiDtos.IntentCardResponse;
import com.example.hackathon_team3_be.api.ApiDtos.PassRecognitionResponse;
import com.example.hackathon_team3_be.api.ApiDtos.PersonalEditResponse;
import com.example.hackathon_team3_be.service.AdvisorService;
import com.example.hackathon_team3_be.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/advisor")
public class AdvisorController {

    private final AdvisorService advisorService;
    private final ReservationService reservationService;

    @GetMapping("/appointments")
    List<AppointmentResponse> appointments(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long storeId
    ) {
        return reservationService.appointments(date == null ? LocalDate.now() : date, storeId);
    }

    @PostMapping("/passes/{passCode}/recognize")
    PassRecognitionResponse recognize(@PathVariable String passCode) {
        return advisorService.recognizePass(passCode);
    }

    @GetMapping("/sessions/{sessionId}/intent-card")
    IntentCardResponse intentCard(@PathVariable UUID sessionId) {
        return advisorService.intentCard(sessionId);
    }

    @PatchMapping("/sessions/{sessionId}/touch")
    IntentCardResponse touch(@PathVariable UUID sessionId, @Valid @RequestBody AdvisorTouchRequest request) {
        return advisorService.advisorTouch(sessionId, request);
    }

    @PostMapping("/sessions/{sessionId}/personal-edits")
    PersonalEditResponse generateEdits(@PathVariable UUID sessionId) {
        return advisorService.generateEdits(sessionId);
    }

    @GetMapping("/sessions/{sessionId}/personal-edits")
    PersonalEditResponse getEdits(@PathVariable UUID sessionId) {
        return advisorService.getEdits(sessionId);
    }
}
