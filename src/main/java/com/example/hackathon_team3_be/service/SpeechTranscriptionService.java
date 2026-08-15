package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.common.InvalidStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class SpeechTranscriptionService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "audio/mpeg", "audio/mp4", "audio/wav", "audio/x-wav", "audio/webm", "audio/ogg"
    );

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${resense.speech.api-url:}")
    private String apiUrl;

    @Value("${resense.speech.api-key:}")
    private String apiKey;

    @Value("${resense.speech.model:gpt-4o-mini-transcribe}")
    private String model;

    public SpeechTranscriptionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TranscriptionResult transcribe(MultipartFile audio, String browserTranscript, String language) {
        String fallback = trimToNull(browserTranscript);
        if (audio != null && !audio.isEmpty()) {
            validateAudio(audio);
            if (configured()) {
                try {
                    return new TranscriptionResult(callProvider(audio, language), "SPEECH_API");
                } catch (Exception exception) {
                    if (fallback != null) {
                        return new TranscriptionResult(fallback, "BROWSER_TRANSCRIPT_FALLBACK");
                    }
                    throw new InvalidStateException("음성 인식 제공자 호출에 실패했습니다. 잠시 후 다시 시도해 주세요.");
                }
            }
        }
        if (fallback != null) {
            return new TranscriptionResult(fallback, "BROWSER_TRANSCRIPT");
        }
        if (audio == null || audio.isEmpty()) {
            throw new InvalidStateException("음성 파일 또는 브라우저 음성 인식 transcript가 필요합니다.");
        }
        throw new InvalidStateException("서버 음성 인식이 설정되지 않았습니다. SPEECH_API_URL/API_KEY를 설정하거나 browserTranscript를 보내 주세요.");
    }

    private boolean configured() {
        return trimToNull(apiUrl) != null && trimToNull(apiKey) != null;
    }

    private void validateAudio(MultipartFile audio) {
        String contentType = audio.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidStateException("지원하는 음성 형식은 mp3, mp4, wav, webm, ogg입니다.");
        }
    }

    private String callProvider(MultipartFile audio, String language) throws Exception {
        String boundary = "----ReSense" + UUID.randomUUID().toString().replace("-", "");
        byte[] body = multipartBody(boundary, audio, language);
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(45))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Speech API returned " + response.statusCode());
        }
        JsonNode json = objectMapper.readTree(response.body());
        String text = trimToNull(json.path("text").asText());
        if (text == null) {
            throw new IllegalStateException("Speech API response has no text");
        }
        return text;
    }

    private byte[] multipartBody(String boundary, MultipartFile audio, String language) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        writeField(output, boundary, "model", model);
        if (trimToNull(language) != null) {
            writeField(output, boundary, "language", language.trim());
        }
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        String filename = audio.getOriginalFilename() == null ? "voice.webm" : audio.getOriginalFilename();
        output.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + safeFilename(filename) + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Type: " + audio.getContentType() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(audio.getBytes());
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
        output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return output.toByteArray();
    }

    private void writeField(ByteArrayOutputStream output, String boundary, String name, String value) throws Exception {
        output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        output.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n")
                .getBytes(StandardCharsets.UTF_8));
        output.write(value.replace("\r", "").replace("\n", "").getBytes(StandardCharsets.UTF_8));
        output.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }

    private String safeFilename(String value) {
        return value.replace("\"", "").replace("\r", "").replace("\n", "");
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record TranscriptionResult(String text, String source) {
    }
}
