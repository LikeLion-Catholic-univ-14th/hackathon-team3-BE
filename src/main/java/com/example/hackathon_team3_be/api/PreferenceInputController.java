package com.example.hackathon_team3_be.api;

import com.example.hackathon_team3_be.api.ApiDtos.ChoiceInputRequest;
import com.example.hackathon_team3_be.api.ApiDtos.ContinuePreferenceRequest;
import com.example.hackathon_team3_be.api.ApiDtos.InputInterpretationResponse;
import com.example.hackathon_team3_be.api.ApiDtos.InputProgressResponse;
import com.example.hackathon_team3_be.api.ApiDtos.PreferenceCatalogResponse;
import com.example.hackathon_team3_be.api.ApiDtos.TextInputRequest;
import com.example.hackathon_team3_be.service.PreferenceInputService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PreferenceInputController {

    private final PreferenceInputService preferenceInputService;

    @GetMapping("/preference-options")
    PreferenceCatalogResponse catalog() {
        return preferenceInputService.catalog();
    }

    @GetMapping("/sessions/{sessionId}/input-progress")
    InputProgressResponse progress(@PathVariable UUID sessionId) {
        return preferenceInputService.progress(sessionId);
    }

    @PostMapping("/sessions/{sessionId}/inputs/choice")
    InputInterpretationResponse choice(
            @PathVariable UUID sessionId,
            @Valid @RequestBody ChoiceInputRequest request
    ) {
        return preferenceInputService.choice(sessionId, request);
    }

    @PostMapping("/sessions/{sessionId}/inputs/text")
    InputInterpretationResponse text(
            @PathVariable UUID sessionId,
            @Valid @RequestBody TextInputRequest request
    ) {
        return preferenceInputService.text(sessionId, request.text());
    }

    @PostMapping(value = "/sessions/{sessionId}/inputs/voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    InputInterpretationResponse voice(
            @PathVariable UUID sessionId,
            @RequestPart(name = "audio", required = false) MultipartFile audio,
            @RequestParam(name = "browserTranscript", required = false) String browserTranscript,
            @RequestParam(name = "language", defaultValue = "ko") String language
    ) {
        return preferenceInputService.voice(sessionId, audio, browserTranscript, language);
    }

    @PostMapping("/sessions/{sessionId}/preferences/continue")
    InputInterpretationResponse continuePrevious(
            @PathVariable UUID sessionId,
            @RequestBody(required = false) ContinuePreferenceRequest request
    ) {
        ContinuePreferenceRequest safeRequest = request == null ? new ContinuePreferenceRequest(null) : request;
        return preferenceInputService.continuePrevious(sessionId, safeRequest);
    }
}
