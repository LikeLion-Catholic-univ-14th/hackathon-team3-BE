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
import org.springframework.beans.factory.annotation.Value;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UnseenGenerationService {

    private final ExperienceSessionRepository sessionRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Value("${resense.image.api-url:}")
    private String imageApiUrl;
    @Value("${resense.image.api-key:}")
    private String imageApiKey;
    @Value("${resense.image.model:gpt-image-2}")
    private String imageModel;
    @Value("${resense.image.max-attempts:2}")
    private int maxAttempts;

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
            GeneratedImage generated = generateWithRetry(prompt, session.getUnseenPublicId());
            session.setUnseenImageUrl(generated.url());
            session.setUnseenImageData(generated.data());
            session.setUnseenImageContentType(generated.contentType());
            session.setUnseenStatus(GenerationStatus.READY);
            session.setStatus(ExperienceStatus.UNSEEN_READY);
        } catch (RuntimeException exception) {
            session.setUnseenStatus(GenerationStatus.FAILED);
            session.setUnseenError("UNSEEN 생성이 지연되고 있습니다. 다시 시도해 주세요.");
            session.setStatus(ExperienceStatus.INTENT_READY);
        }
    }

    private GeneratedImage generateWithRetry(String prompt, String unseenId) {
        if (imageApiUrl.isBlank() || imageApiKey.isBlank()) {
            return fallback(unseenId);
        }
        for (int attempt = 1; attempt <= Math.max(1, maxAttempts); attempt++) {
            try {
                return callImageProvider(prompt, unseenId);
            } catch (Exception ignored) {
                // Retry; the deterministic SVG below keeps the customer journey available.
            }
        }
        return fallback(unseenId);
    }

    private GeneratedImage callImageProvider(String prompt, String unseenId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "model", imageModel, "prompt", prompt, "size", "1536x1024", "n", 1
        ));
        HttpRequest request = HttpRequest.newBuilder(URI.create(imageApiUrl))
                .timeout(Duration.ofSeconds(50))
                .header("Authorization", "Bearer " + imageApiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Image provider returned HTTP " + response.statusCode());
        }
        JsonNode first = objectMapper.readTree(response.body()).path("data").path(0);
        String url = first.path("url").asText("");
        if (!url.isBlank()) return new GeneratedImage(url, null, null);
        String base64 = first.path("b64_json").asText("");
        if (base64.isBlank()) throw new IllegalStateException("Image provider returned no image");
        byte[] png = Base64.getDecoder().decode(base64);
        if (png.length == 0) throw new IllegalStateException("Image provider returned an empty image");
        return new GeneratedImage("/api/v1/assets/unseen/" + unseenId + ".png", png, "image/png");
    }

    private GeneratedImage fallback(String unseenId) {
        return new GeneratedImage("/api/v1/assets/unseen/" + unseenId + ".svg", null, null);
    }

    private record GeneratedImage(String url, byte[] data, String contentType) {
    }
}
