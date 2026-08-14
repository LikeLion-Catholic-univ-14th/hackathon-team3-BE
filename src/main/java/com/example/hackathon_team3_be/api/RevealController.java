package com.example.hackathon_team3_be.api;

import com.example.hackathon_team3_be.api.ApiDtos.RevealResponse;
import com.example.hackathon_team3_be.service.RevealService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reveal/sessions/{sessionId}")
public class RevealController {

    private final RevealService revealService;

    @PostMapping("/start")
    RevealResponse start(@PathVariable UUID sessionId) {
        return revealService.start(sessionId);
    }

    @PostMapping("/advance")
    RevealResponse advance(@PathVariable UUID sessionId) {
        return revealService.advance(sessionId);
    }

    @GetMapping
    RevealResponse state(@PathVariable UUID sessionId) {
        return revealService.get(sessionId);
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    SseEmitter events(@PathVariable UUID sessionId) {
        return revealService.subscribe(sessionId);
    }
}
