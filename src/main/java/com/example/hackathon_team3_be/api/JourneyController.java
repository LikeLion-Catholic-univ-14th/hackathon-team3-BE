package com.example.hackathon_team3_be.api;

import com.example.hackathon_team3_be.api.ApiDtos.CreateSessionRequest;
import com.example.hackathon_team3_be.api.ApiDtos.CustomerMemoryResponse;
import com.example.hackathon_team3_be.api.ApiDtos.FeedbackRequest;
import com.example.hackathon_team3_be.api.ApiDtos.FeedbackResponse;
import com.example.hackathon_team3_be.api.ApiDtos.IntentProfileResponse;
import com.example.hackathon_team3_be.api.ApiDtos.PreferenceRequest;
import com.example.hackathon_team3_be.api.ApiDtos.SessionResponse;
import com.example.hackathon_team3_be.api.ApiDtos.SelectUnseenCandidateRequest;
import com.example.hackathon_team3_be.api.ApiDtos.UnseenResponse;
import com.example.hackathon_team3_be.service.JourneyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class JourneyController {

    private final JourneyService journeyService;

    @PostMapping("/sessions")
    ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(journeyService.createSession(request));
    }

    @GetMapping("/sessions/{sessionId}")
    SessionResponse getSession(@PathVariable UUID sessionId) {
        return journeyService.getSession(sessionId);
    }

    @PutMapping("/sessions/{sessionId}/preferences")
    SessionResponse savePreferences(@PathVariable UUID sessionId, @Valid @RequestBody PreferenceRequest request) {
        return journeyService.savePreferences(sessionId, request);
    }

    @PostMapping("/sessions/{sessionId}/intent")
    IntentProfileResponse interpret(@PathVariable UUID sessionId) {
        return journeyService.interpret(sessionId);
    }

    @PostMapping("/sessions/{sessionId}/unseen")
    ResponseEntity<UnseenResponse> requestUnseen(@PathVariable UUID sessionId) {
        return ResponseEntity.accepted().body(journeyService.requestUnseen(sessionId));
    }

    @GetMapping("/sessions/{sessionId}/unseen")
    UnseenResponse getUnseen(@PathVariable UUID sessionId) {
        return journeyService.getUnseen(sessionId);
    }

    @PatchMapping("/sessions/{sessionId}/unseen/selection")
    UnseenResponse selectUnseenCandidate(
            @PathVariable UUID sessionId,
            @Valid @RequestBody SelectUnseenCandidateRequest request
    ) {
        return journeyService.selectUnseenCandidate(sessionId, request);
    }

    @PostMapping("/sessions/{sessionId}/feedback")
    FeedbackResponse feedback(@PathVariable UUID sessionId, @Valid @RequestBody FeedbackRequest request) {
        return journeyService.saveFeedback(sessionId, request);
    }

    @GetMapping("/customers/{demoCustomerId}/memory")
    CustomerMemoryResponse memory(@PathVariable String demoCustomerId) {
        return journeyService.getMemory(demoCustomerId);
    }
}
