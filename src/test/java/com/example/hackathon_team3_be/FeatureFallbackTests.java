package com.example.hackathon_team3_be;

import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.repository.ExperienceSessionRepository;
import com.example.hackathon_team3_be.service.AiIntentInterpreter;
import com.example.hackathon_team3_be.service.IntentInterpreter;
import com.example.hackathon_team3_be.service.RuleBasedIntentInterpreter;
import com.example.hackathon_team3_be.service.UnseenGenerationService;
import com.example.hackathon_team3_be.service.UnseenRequestedEvent;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FeatureFallbackTests {

    @Autowired MockMvc mockMvc;
    @Autowired ExperienceSessionRepository sessionRepository;

    @Test
    void storesAreSortedByDistanceWhenCoordinatesAreProvided() throws Exception {
        mockMvc.perform(get("/api/v1/stores").param("latitude", "37.5274").param("longitude", "127.0438"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("MCM HAUS SEOUL"))
                .andExpect(jsonPath("$[0].distanceKm").value(0.0));
    }

    @Test
    void missingAiConfigurationUsesRuleBasedIntentFallback() {
        AiIntentInterpreter interpreter = new AiIntentInterpreter(new RuleBasedIntentInterpreter(), new ObjectMapper());
        ReflectionTestUtils.setField(interpreter, "apiUrl", "");
        ReflectionTestUtils.setField(interpreter, "apiKey", "");
        ExperienceSession session = preferenceSession();

        IntentInterpreter.IntentResult result = interpreter.interpret(session);

        assertThat(result.priority()).isEqualTo("Mobility / Lightweight");
        assertThat(result.signature()).contains("Soft", "Cognac");
    }

    @Test
    void missingImageConfigurationUsesSvgFallback() {
        ExperienceSessionRepository repository = mock(ExperienceSessionRepository.class);
        ObjectMapper mapper = new ObjectMapper();
        UnseenGenerationService service = new UnseenGenerationService(repository, mapper);
        ReflectionTestUtils.setField(service, "imageApiUrl", "");
        ReflectionTestUtils.setField(service, "imageApiKey", "");
        ExperienceSession session = preferenceSession();
        UUID sessionId = UUID.randomUUID();
        session.setUnseenPublicId("UNSEEN-TEST");
        when(repository.findById(sessionId)).thenReturn(Optional.of(session));

        service.generate(new UnseenRequestedEvent(sessionId));

        assertThat(session.getUnseenImageUrl()).isEqualTo("/api/v1/assets/unseen/UNSEEN-TEST.svg");
    }

    @Test
    void generatedPngIsServedFromTheAssetEndpoint() throws Exception {
        ExperienceSession session = preferenceSession();
        session.setDemoCustomerId("png-test");
        session.setCustomerName("PNG Tester");
        session.setUnseenPublicId("UNSEEN-PNGTEST");
        session.setUnseenImageData(new byte[]{(byte) 0x89, 0x50, 0x4e, 0x47});
        session.setUnseenImageContentType("image/png");
        sessionRepository.saveAndFlush(session);

        mockMvc.perform(get("/api/v1/assets/unseen/UNSEEN-PNGTEST.png"))
                .andExpect(status().isOk())
                .andExpect(result -> assertThat(result.getResponse().getContentType()).startsWith("image/png"))
                .andExpect(result -> assertThat(result.getResponse().getContentAsByteArray()).hasSize(4));
    }

    private ExperienceSession preferenceSession() {
        ExperienceSession session = new ExperienceSession();
        session.setSilhouette("Crossbody");
        session.setStructurePreference("Soft");
        session.setProportion("Balanced");
        session.setColor("Cognac");
        session.setAttitude("Quiet");
        session.setContexts("Work|Travel");
        session.setLockedAttribute("Shape");
        return session;
    }
}
