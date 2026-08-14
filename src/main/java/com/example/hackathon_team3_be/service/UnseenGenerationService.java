package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.GenerationStatus;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.repository.ExperienceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@RequiredArgsConstructor
public class UnseenGenerationService {

    private final ExperienceSessionRepository sessionRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void generate(UnseenRequestedEvent event) {
        ExperienceSession session = sessionRepository.findById(event.sessionId()).orElse(null);
        if (session == null) {
            return;
        }
        try {
            String prompt = "MCM-inspired luxury bag concept, %s silhouette, %s structure, %s proportion, %s color, %s attitude, for %s. Editorial product render, quiet luxury, no logo."
                    .formatted(
                            session.getSilhouette(),
                            session.getStructurePreference(),
                            session.getProportion(),
                            session.getColor(),
                            session.getAttitude(),
                            session.getContexts().replace('|', ' ')
                    );
            session.setUnseenPrompt(prompt);
            session.setUnseenImageUrl("/api/v1/assets/unseen/" + session.getUnseenPublicId() + ".svg");
            session.setUnseenStatus(GenerationStatus.READY);
            session.setStatus(ExperienceStatus.UNSEEN_READY);
        } catch (RuntimeException exception) {
            session.setUnseenStatus(GenerationStatus.FAILED);
            session.setUnseenError("UNSEEN 생성이 지연되고 있습니다. 다시 시도해 주세요.");
            session.setStatus(ExperienceStatus.INTENT_READY);
        }
    }
}
