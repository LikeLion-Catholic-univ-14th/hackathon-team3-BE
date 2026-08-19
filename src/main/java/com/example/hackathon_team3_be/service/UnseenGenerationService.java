package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.GenerationStatus;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.domain.UnseenCandidate;
import com.example.hackathon_team3_be.repository.ExperienceSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UnseenGenerationService {

    private final ExperienceSessionRepository sessionRepository;
    private final List<String> demoImages;

    public UnseenGenerationService(
            ExperienceSessionRepository sessionRepository,
            @Value("${resense.unseen.demo-images:}") String demoImages
    ) {
        this.sessionRepository = sessionRepository;
        this.demoImages = Arrays.stream(demoImages.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

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
            List<CandidateTemplate> candidates = createCandidates(session);
            session.getUnseenCandidates().clear();
            for (int index = 0; index < candidates.size(); index++) {
                CandidateTemplate template = candidates.get(index);
                UnseenCandidate candidate = new UnseenCandidate();
                candidate.setSession(session);
                candidate.setImageUrl(template.imageUrl());
                candidate.setShape(template.shape());
                candidate.setSize(template.size());
                candidate.setColor(template.color());
                candidate.setRank(index + 1);
                candidate.setSelected(candidates.size() == 1);
                session.getUnseenCandidates().add(candidate);
            }
            session.setUnseenImageUrl(candidates.get(0).imageUrl());
            session.setUnseenStatus(GenerationStatus.READY);
            session.setStatus(ExperienceStatus.UNSEEN_READY);
        } catch (RuntimeException exception) {
            session.setUnseenStatus(GenerationStatus.FAILED);
            session.setUnseenError("UNSEEN 생성이 지연되고 있습니다. 다시 시도해 주세요.");
            session.setStatus(ExperienceStatus.INTENT_READY);
        }
    }

    private List<CandidateTemplate> createCandidates(ExperienceSession session) {
        if (demoImages.isEmpty()) {
            return List.of(new CandidateTemplate(
                    "/api/v1/assets/unseen/" + session.getUnseenPublicId() + ".svg",
                    session.getSilhouette(), sizeFor(session.getProportion()), session.getColor()
            ));
        }
        List<CandidateTemplate> templates = new ArrayList<>(demoImages.stream()
                .map(url -> templateFor(url, session))
                .toList());
        Collections.shuffle(templates);
        return templates.subList(0, Math.min(4, templates.size()));
    }

    private CandidateTemplate templateFor(String imageUrl, ExperienceSession session) {
        if (imageUrl.endsWith("/image.png")) {
            return new CandidateTemplate(imageUrl, "Crossbody", "Mini", "Cognac");
        }
        if (imageUrl.endsWith("/image2.png")) {
            return new CandidateTemplate(imageUrl, "Crossbody", "Mini", "Black");
        }
        if (imageUrl.endsWith("/image3.png")) {
            return new CandidateTemplate(imageUrl, "Crossbody", "Mini", "Cream");
        }
        if (imageUrl.endsWith("/image4.png")) {
            return new CandidateTemplate(imageUrl, "Top Handle", "Mini", "Cognac");
        }
        if (imageUrl.endsWith("/image5.png")) {
            return new CandidateTemplate(imageUrl, "Bowler", "Medium", "Black");
        }
        if (imageUrl.endsWith("/image6.png")) {
            return new CandidateTemplate(imageUrl, "Tote", "Large", "Cream / Cognac");
        }
        if (imageUrl.endsWith("/image7.png")) {
            return new CandidateTemplate(imageUrl, "Hobo", "Medium", "Blue");
        }
        if (imageUrl.endsWith("/image8.png")) {
            return new CandidateTemplate(imageUrl, "Top Handle", "Mini", "Cognac");
        }
        return new CandidateTemplate(imageUrl, session.getSilhouette(), sizeFor(session.getProportion()), session.getColor());
    }

    private String sizeFor(String proportion) {
        if (proportion == null) return "Medium";
        return switch (proportion.toLowerCase()) {
            case "compact" -> "Mini";
            case "spacious" -> "Large";
            default -> "Medium";
        };
    }

    private record CandidateTemplate(String imageUrl, String shape, String size, String color) {
    }
}
