package com.example.hackathon_team3_be;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UnseenRandomImageIntegrationTests {

    private static final Set<String> DEMO_IMAGES = Set.of(
            "/assets/unseen/image.png",
            "/assets/unseen/image2.png",
            "/assets/unseen/image3.png",
            "/assets/unseen/image4.png",
            "/assets/unseen/image5.png",
            "/assets/unseen/image6.png",
            "/assets/unseen/image7.png",
            "/assets/unseen/image8.png"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void figmaProfileLockOptionsAndRandomUnseenImageWorkEndToEnd() throws Exception {
        mockMvc.perform(get("/api/v1/preference-options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps[6].options[*].value",
                        contains("Shape", "Color", "Space", "Attitude")));

        String createBody = """
                {
                  "demoCustomerId": "figma-demo-user",
                  "customerName": "Lena",
                  "phone": "010-1234-5678",
                  "email": "lena@example.com",
                  "gender": "FEMALE",
                  "dataConsent": true
                }
                """;
        String createResponse = mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("010-1234-5678"))
                .andExpect(jsonPath("$.email").value("lena@example.com"))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = objectMapper.readTree(createResponse).path("sessionId").asText();

        mockMvc.perform(put("/api/v1/sessions/{sessionId}/preferences", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "silhouette": "Crossbody",
                                  "structure": "Soft",
                                  "proportion": "Compact",
                                  "color": "Cognac",
                                  "attitude": "Quiet",
                                  "contexts": ["Work", "Travel"],
                                  "lockedAttribute": "Space"
                                }
                                """))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/v1/sessions/{sessionId}/intent", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lockedAttribute").value("Space"));
        mockMvc.perform(post("/api/v1/sessions/{sessionId}/unseen", sessionId))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.unseenId", startsWith("UNSEEN-")));

        String imageUrl = awaitReadyImage(sessionId);
        if (!DEMO_IMAGES.contains(imageUrl)) {
            throw new AssertionError("Configured demo image was not selected: " + imageUrl);
        }
        mockMvc.perform(get(imageUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));
    }

    private String awaitReadyImage(String sessionId) throws Exception {
        for (int attempt = 0; attempt < 100; attempt++) {
            String body = mockMvc.perform(get("/api/v1/sessions/{sessionId}/unseen", sessionId))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            JsonNode response = objectMapper.readTree(body);
            if ("READY".equals(response.path("status").asText())) {
                return response.path("imageUrl").asText();
            }
            Thread.sleep(20);
        }
        throw new AssertionError("UNSEEN generation did not become READY");
    }
}
