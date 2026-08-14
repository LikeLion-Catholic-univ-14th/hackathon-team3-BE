package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.api.ApiDtos.RevealResponse;
import com.example.hackathon_team3_be.common.InvalidStateException;
import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.RevealStage;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@RequiredArgsConstructor
public class RevealService {

    private static final List<RevealStage> FLOW = List.of(
            RevealStage.WELCOME,
            RevealStage.UNSEEN_REVEAL,
            RevealStage.LIFESTYLE_SCENE,
            RevealStage.FINAL_TRANSITION,
            RevealStage.COMPLETED
    );

    private final JourneyService journeyService;
    private final Map<UUID, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @Transactional
    public RevealResponse start(UUID sessionId) {
        ExperienceSession session = journeyService.findSession(sessionId);
        if (session.getUnseenPublicId() == null) {
            throw new InvalidStateException("UNSEEN이 없어 Reveal을 시작할 수 없습니다.");
        }
        session.setRevealStage(RevealStage.WELCOME);
        session.setStatus(ExperienceStatus.REVEALING);
        RevealResponse response = response(session);
        publish(sessionId, response);
        return response;
    }

    @Transactional
    public RevealResponse advance(UUID sessionId) {
        ExperienceSession session = journeyService.findSession(sessionId);
        int current = FLOW.indexOf(session.getRevealStage());
        if (current < 0) {
            throw new InvalidStateException("Reveal을 먼저 시작해 주세요.");
        }
        RevealStage next = current == FLOW.size() - 1 ? RevealStage.COMPLETED : FLOW.get(current + 1);
        session.setRevealStage(next);
        if (next == RevealStage.COMPLETED && session.getStatus() == ExperienceStatus.REVEALING) {
            session.setStatus(ExperienceStatus.PERSONAL_EDIT_READY);
        }
        RevealResponse response = response(session);
        publish(sessionId, response);
        return response;
    }

    @Transactional(readOnly = true)
    public RevealResponse get(UUID sessionId) {
        return response(journeyService.findSession(sessionId));
    }

    public SseEmitter subscribe(UUID sessionId) {
        RevealResponse current = get(sessionId);
        SseEmitter emitter = new SseEmitter(0L);
        emitters.computeIfAbsent(sessionId, ignored -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> remove(sessionId, emitter));
        emitter.onTimeout(() -> remove(sessionId, emitter));
        emitter.onError(error -> remove(sessionId, emitter));
        try {
            emitter.send(SseEmitter.event().name("reveal-state").data(current));
        } catch (IOException exception) {
            remove(sessionId, emitter);
        }
        return emitter;
    }

    private void publish(UUID sessionId, RevealResponse response) {
        List<SseEmitter> sessionEmitters = emitters.getOrDefault(sessionId, new CopyOnWriteArrayList<>());
        for (SseEmitter emitter : sessionEmitters) {
            try {
                emitter.send(SseEmitter.event().name("reveal-state").data(response));
                if (response.stage() == RevealStage.COMPLETED) {
                    emitter.complete();
                }
            } catch (IOException | IllegalStateException exception) {
                remove(sessionId, emitter);
            }
        }
    }

    private void remove(UUID sessionId, SseEmitter emitter) {
        List<SseEmitter> sessionEmitters = emitters.get(sessionId);
        if (sessionEmitters != null) {
            sessionEmitters.remove(emitter);
            if (sessionEmitters.isEmpty()) {
                emitters.remove(sessionId);
            }
        }
    }

    private RevealResponse response(ExperienceSession session) {
        return new RevealResponse(session.getId(), session.getRevealStage(), session.getStatus());
    }
}
