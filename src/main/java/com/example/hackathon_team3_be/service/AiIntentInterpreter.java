package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.domain.ExperienceSession;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Primary
@Component
@RequiredArgsConstructor
public class AiIntentInterpreter implements IntentInterpreter {

    private final RuleBasedIntentInterpreter fallback;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Value("${resense.intent.api-url:}")
    private String apiUrl;
    @Value("${resense.intent.api-key:}")
    private String apiKey;
    @Value("${resense.intent.model:gpt-4o-mini}")
    private String model;
    @Value("${resense.intent.max-attempts:2}")
    private int maxAttempts;

    @Override
    public IntentResult interpret(ExperienceSession session) {
        if (apiUrl.isBlank() || apiKey.isBlank()) {
            return fallback.interpret(session);
        }
        RuntimeException last = null;
        for (int attempt = 1; attempt <= Math.max(1, maxAttempts); attempt++) {
            try {
                return callProvider(session);
            } catch (Exception exception) {
                last = new IllegalStateException("AI Intent provider failed on attempt " + attempt, exception);
            }
        }
        return fallback.interpret(session);
    }

    private IntentResult callProvider(ExperienceSession session) throws Exception {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "purpose", stringSchema(), "priority", stringSchema(), "style", stringSchema(),
                        "signature", stringSchema(), "concern", stringSchema(), "summary", stringSchema()
                ),
                "required", List.of("purpose", "priority", "style", "signature", "concern", "summary"),
                "additionalProperties", false
        );
        String input = """
                Convert this luxury retail preference into a concise customer Intent Profile.
                Never invent personal facts. Keep purpose/priority/style/signature/concern short and summary in Korean.
                silhouette=%s; structure=%s; proportion=%s; color=%s; attitude=%s; contexts=%s; lockedAttribute=%s
                """.formatted(session.getSilhouette(), session.getStructurePreference(), session.getProportion(),
                session.getColor(), session.getAttitude(), session.getContexts(), session.getLockedAttribute());
        Map<String, Object> body = Map.of(
                "model", model,
                "input", List.of(
                        Map.of("role", "system", "content", "You create structured purchase-intent profiles for an MCM luxury retail advisor."),
                        Map.of("role", "user", "content", input)
                ),
                "text", Map.of("format", Map.of(
                        "type", "json_schema", "name", "intent_profile", "strict", true, "schema", schema
                ))
        );
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Intent provider returned HTTP " + response.statusCode());
        }
        JsonNode root = objectMapper.readTree(response.body());
        String json = findOutputText(root);
        JsonNode result = objectMapper.readTree(json);
        return new IntentResult(required(result, "purpose"), required(result, "priority"), required(result, "style"),
                required(result, "signature"), required(result, "concern"), required(result, "summary"));
    }

    private Map<String, String> stringSchema() {
        return Map.of("type", "string");
    }

    private String findOutputText(JsonNode root) {
        JsonNode direct = root.get("output_text");
        if (direct != null && direct.isTextual()) return direct.asText();
        JsonNode output = root.path("output");
        if (output.isArray()) {
            for (JsonNode item : output) {
                for (JsonNode content : item.path("content")) {
                    JsonNode text = content.get("text");
                    if (text != null && text.isTextual()) return text.asText();
                }
            }
        }
        throw new IllegalStateException("Intent provider response has no output text");
    }

    private String required(JsonNode node, String field) {
        String value = node.path(field).asText("").trim();
        if (value.isEmpty()) throw new IllegalStateException("Missing AI Intent field: " + field);
        return value;
    }
}
