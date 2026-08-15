package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.api.ApiDtos.CreateSessionRequest;
import com.example.hackathon_team3_be.api.ApiDtos.CustomerMemoryResponse;
import com.example.hackathon_team3_be.api.ApiDtos.FeedbackRequest;
import com.example.hackathon_team3_be.api.ApiDtos.FeedbackResponse;
import com.example.hackathon_team3_be.api.ApiDtos.IntentProfileResponse;
import com.example.hackathon_team3_be.api.ApiDtos.MemoryItem;
import com.example.hackathon_team3_be.api.ApiDtos.PreferenceRequest;
import com.example.hackathon_team3_be.api.ApiDtos.SessionResponse;
import com.example.hackathon_team3_be.api.ApiDtos.UnseenResponse;
import com.example.hackathon_team3_be.common.InvalidStateException;
import com.example.hackathon_team3_be.common.NotFoundException;
import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.GenerationStatus;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.repository.ExperienceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JourneyService {

    private final ExperienceSessionRepository sessionRepository;
    private final IntentInterpreter intentInterpreter;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        ExperienceSession session = new ExperienceSession();
        session.setDemoCustomerId(request.demoCustomerId().trim());
        session.setCustomerName(request.customerName().trim());
        session.setDataConsent(request.dataConsent());
        return ApiMapper.toSession(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public SessionResponse getSession(UUID sessionId) {
        return ApiMapper.toSession(findSession(sessionId));
    }

    @Transactional
    public SessionResponse savePreferences(UUID sessionId, PreferenceRequest request) {
        ExperienceSession session = findSession(sessionId);
        if (session.getStatus().ordinal() >= ExperienceStatus.RESERVED.ordinal()) {
            throw new InvalidStateException("예약 이후에는 취향 입력을 변경할 수 없습니다.");
        }
        session.setSilhouette(request.silhouette().trim());
        session.setStructurePreference(request.structure().trim());
        session.setProportion(request.proportion().trim());
        session.setColor(request.color().trim());
        session.setAttitude(request.attitude().trim());
        session.setContexts(String.join("|", request.contexts()));
        session.setLockedAttribute(request.lockedAttribute().trim());
        session.setStatus(ExperienceStatus.PREFERENCES_SAVED);
        clearDownstreamResults(session);
        return ApiMapper.toSession(session);
    }

    @Transactional
    public IntentProfileResponse interpret(UUID sessionId) {
        ExperienceSession session = findSession(sessionId);
        requirePreferences(session);
        IntentInterpreter.IntentResult result = intentInterpreter.interpret(session);
        session.setIntentPurpose(result.purpose());
        session.setIntentPriority(result.priority());
        session.setIntentStyle(result.style());
        session.setIntentSignature(result.signature());
        session.setIntentConcern(result.concern());
        session.setIntentSummary(result.summary());
        session.setStatus(ExperienceStatus.INTENT_READY);
        return ApiMapper.toIntent(session);
    }

    @Transactional
    public UnseenResponse requestUnseen(UUID sessionId) {
        ExperienceSession session = findSession(sessionId);
        if (session.getIntentPurpose() == null) {
            throw new InvalidStateException("Intent Profile을 먼저 생성해 주세요.");
        }
        if (session.getUnseenStatus() == GenerationStatus.PROCESSING) {
            return ApiMapper.toUnseen(session);
        }
        String publicId = session.getUnseenPublicId() == null
                ? "UNSEEN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                : session.getUnseenPublicId();
        session.setUnseenPublicId(publicId);
        session.setUnseenStatus(GenerationStatus.PROCESSING);
        session.setUnseenError(null);
        session.setStatus(ExperienceStatus.UNSEEN_PROCESSING);
        sessionRepository.save(session);
        eventPublisher.publishEvent(new UnseenRequestedEvent(sessionId));
        return ApiMapper.toUnseen(session);
    }

    @Transactional(readOnly = true)
    public UnseenResponse getUnseen(UUID sessionId) {
        return ApiMapper.toUnseen(findSession(sessionId));
    }

    @Transactional
    public FeedbackResponse saveFeedback(UUID sessionId, FeedbackRequest request) {
        ExperienceSession session = findSession(sessionId);
        session.setFeedbackLoved(join(request.loved()));
        session.setFeedbackConcern(join(request.concerns()));
        session.setFeedbackWants(join(request.wants()));
        session.setPurchaseResult(request.result());
        session.setPurchasedProductId(request.purchasedProductId());
        session.setStatus(ExperienceStatus.COMPLETED);
        return new FeedbackResponse(sessionId, request.result(), session.getStatus());
    }

    @Transactional(readOnly = true)
    public CustomerMemoryResponse getMemory(String demoCustomerId) {
        List<MemoryItem> items = sessionRepository.findByDemoCustomerIdOrderByCreatedAtDesc(demoCustomerId).stream()
                .filter(ExperienceSession::isDataConsent)
                .filter(session -> session.getPurchaseResult() != null)
                .map(session -> new MemoryItem(
                        session.getId(),
                        session.getUnseenPublicId(),
                        session.getIntentSummary(),
                        ApiMapper.split(session.getFeedbackLoved()),
                        ApiMapper.split(session.getFeedbackConcern()),
                        ApiMapper.split(session.getFeedbackWants()),
                        session.getPurchaseResult(),
                        session.getUpdatedAt()
                ))
                .toList();
        return new CustomerMemoryResponse(demoCustomerId, items);
    }

    @Transactional(readOnly = true)
    public ExperienceSession findSession(UUID sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("세션을 찾을 수 없습니다: " + sessionId));
    }

    private void requirePreferences(ExperienceSession session) {
        if (session.getSilhouette() == null
                || session.getStructurePreference() == null
                || session.getProportion() == null
                || session.getColor() == null
                || session.getAttitude() == null
                || session.getContexts() == null
                || session.getLockedAttribute() == null) {
            throw new InvalidStateException("취향 정보를 먼저 저장해 주세요.");
        }
    }

    private void clearDownstreamResults(ExperienceSession session) {
        session.setIntentPurpose(null);
        session.setIntentPriority(null);
        session.setIntentStyle(null);
        session.setIntentSignature(null);
        session.setIntentConcern(null);
        session.setIntentSummary(null);
        session.setUnseenStatus(GenerationStatus.NOT_STARTED);
        session.setUnseenPublicId(null);
        session.setUnseenImageUrl(null);
        session.setUnseenPrompt(null);
        session.setUnseenError(null);
        session.setAdvisorPriority(null);
        session.setRevealStage(com.example.hackathon_team3_be.domain.DomainEnums.RevealStage.NOT_STARTED);
    }

    private String join(List<String> values) {
        return values == null ? null : String.join("|", values);
    }
}
