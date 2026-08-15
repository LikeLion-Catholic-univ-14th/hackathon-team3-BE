package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.api.ApiDtos.ChoiceInputRequest;
import com.example.hackathon_team3_be.api.ApiDtos.ChoiceOptionResponse;
import com.example.hackathon_team3_be.api.ApiDtos.ChoiceStepResponse;
import com.example.hackathon_team3_be.api.ApiDtos.ContinuePreferenceRequest;
import com.example.hackathon_team3_be.api.ApiDtos.InputInterpretationResponse;
import com.example.hackathon_team3_be.api.ApiDtos.InputProgressResponse;
import com.example.hackathon_team3_be.api.ApiDtos.PreferenceCatalogResponse;
import com.example.hackathon_team3_be.common.InvalidStateException;
import com.example.hackathon_team3_be.common.NotFoundException;
import com.example.hackathon_team3_be.domain.DomainEnums.ExperienceStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.GenerationStatus;
import com.example.hackathon_team3_be.domain.DomainEnums.InputMode;
import com.example.hackathon_team3_be.domain.DomainEnums.InputStep;
import com.example.hackathon_team3_be.domain.DomainEnums.RevealStage;
import com.example.hackathon_team3_be.domain.ExperienceSession;
import com.example.hackathon_team3_be.repository.ExperienceSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PreferenceInputService {

    private static final List<ChoiceStepResponse> STEPS = List.of(
            step(InputStep.SILHOUETTE, "실루엣", "어떤 형태가 가장 먼저 눈에 들어오나요?", false,
                    option("Crossbody", "크로스바디"), option("Tote", "토트"), option("Hobo", "호보"), option("Backpack", "백팩")),
            step(InputStep.STRUCTURE, "구조감", "가방의 형태감은 어느 쪽이 편한가요?", false,
                    option("Soft", "부드러운"), option("Semi-structured", "적당한 구조감"), option("Structured", "각이 잡힌")),
            step(InputStep.PROPORTION, "수납 비율", "평소 필요한 수납 크기를 골라 주세요.", false,
                    option("Compact", "컴팩트"), option("Balanced", "균형형"), option("Spacious", "넉넉한")),
            step(InputStep.COLOR, "컬러", "오늘 가장 끌리는 컬러는 무엇인가요?", false,
                    option("Cognac", "코냑 브라운"), option("Black", "블랙"), option("Cream", "크림"), option("Red", "레드")),
            step(InputStep.ATTITUDE, "무드", "원하는 인상을 골라 주세요.", false,
                    option("Quiet", "차분한"), option("Balanced", "절제된 포인트"), option("Expressive", "대담한")),
            step(InputStep.CONTEXTS, "사용 장면", "어떤 장면에서 주로 들고 싶나요? (복수 선택)", true,
                    option("Work", "출근/업무"), option("Daily", "일상"), option("Weekend", "주말"), option("Travel", "여행")),
            step(InputStep.LOCKED_ATTRIBUTE, "꼭 지킬 한 가지", "추천에서 절대 놓치고 싶지 않은 조건은 무엇인가요?", false,
                    option("Shape", "형태"), option("Weight", "무게"), option("Color", "색상"), option("Capacity", "수납력"), option("Styling", "스타일링"))
    );

    private final JourneyService journeyService;
    private final ExperienceSessionRepository sessionRepository;
    private final SpeechTranscriptionService speechTranscriptionService;

    public PreferenceCatalogResponse catalog() {
        return new PreferenceCatalogResponse(STEPS);
    }

    @Transactional(readOnly = true)
    public InputProgressResponse progress(UUID sessionId) {
        return toProgress(journeyService.findSession(sessionId));
    }

    @Transactional
    public InputInterpretationResponse choice(UUID sessionId, ChoiceInputRequest request) {
        ExperienceSession session = journeyService.findSession(sessionId);
        requireEditable(session);
        List<String> canonical = validateAndCanonicalize(request.step(), request.values());
        apply(session, request.step(), canonical);
        session.setLastInputMode(InputMode.CHOICE);
        resetDownstream(session);
        return response(session, InputMode.CHOICE, null, null,
                Map.of(request.step().name(), canonical), List.of(request.step().name()));
    }

    @Transactional
    public InputInterpretationResponse text(UUID sessionId, String text) {
        return applyNaturalLanguage(sessionId, text.trim(), InputMode.TEXT, null);
    }

    @Transactional
    public InputInterpretationResponse voice(
            UUID sessionId,
            MultipartFile audio,
            String browserTranscript,
            String language
    ) {
        SpeechTranscriptionService.TranscriptionResult result =
                speechTranscriptionService.transcribe(audio, browserTranscript, language);
        InputInterpretationResponse response = applyNaturalLanguage(
                sessionId, result.text(), InputMode.VOICE, result.source()
        );
        ExperienceSession session = journeyService.findSession(sessionId);
        session.setVoiceTranscript(result.text());
        return response;
    }

    @Transactional
    public InputInterpretationResponse continuePrevious(
            UUID sessionId,
            ContinuePreferenceRequest request
    ) {
        ExperienceSession target = journeyService.findSession(sessionId);
        requireEditable(target);
        if (!target.isDataConsent()) {
            throw new InvalidStateException("이전 취향 이어가기는 데이터 활용 동의가 필요합니다.");
        }
        ExperienceSession source = findSource(target, request.sourceSessionId());
        copyPreferences(source, target);
        target.setLastInputMode(InputMode.PREVIOUS_SESSION);
        resetDownstream(target);
        return response(target, InputMode.PREVIOUS_SESSION, null, null, Map.of(),
                List.of("SILHOUETTE", "STRUCTURE", "PROPORTION", "COLOR", "ATTITUDE", "CONTEXTS", "LOCKED_ATTRIBUTE"));
    }

    private InputInterpretationResponse applyNaturalLanguage(
            UUID sessionId,
            String text,
            InputMode mode,
            String source
    ) {
        ExperienceSession session = journeyService.findSession(sessionId);
        requireEditable(session);
        Map<InputStep, List<String>> extracted = extract(text);
        List<String> applied = new ArrayList<>();
        for (Map.Entry<InputStep, List<String>> entry : extracted.entrySet()) {
            if (isMissing(session, entry.getKey())) {
                apply(session, entry.getKey(), entry.getValue());
                applied.add(entry.getKey().name());
            }
        }
        session.setLastInputMode(mode);
        if (mode == InputMode.TEXT) {
            session.setFreeTextInput(text);
        }
        if (!applied.isEmpty()) {
            resetDownstream(session);
        }
        Map<String, List<String>> exposed = new LinkedHashMap<>();
        extracted.forEach((key, value) -> exposed.put(key.name(), value));
        return response(session, mode, text, source, exposed, applied);
    }

    private Map<InputStep, List<String>> extract(String text) {
        String normalized = text.toLowerCase(Locale.ROOT);
        Map<InputStep, List<String>> result = new EnumMap<>(InputStep.class);
        matchOne(result, InputStep.SILHOUETTE, normalized,
                Map.of("Crossbody", List.of("crossbody", "크로스바디"), "Tote", List.of("tote", "토트"),
                        "Hobo", List.of("hobo", "호보"), "Backpack", List.of("backpack", "백팩")));
        matchOne(result, InputStep.STRUCTURE, normalized,
                Map.of("Soft", List.of("soft", "부드러", "부드럽"), "Semi-structured", List.of("semi", "적당한 구조"),
                        "Structured", List.of("structured", "각이 잡", "각진")));
        matchOne(result, InputStep.PROPORTION, normalized,
                Map.of("Compact", List.of("compact", "컴팩트", "작은"), "Balanced", List.of("balanced", "균형"),
                        "Spacious", List.of("spacious", "넉넉", "수납이 큰")));
        matchOne(result, InputStep.COLOR, normalized,
                Map.of("Cognac", List.of("cognac", "코냑", "브라운"), "Black", List.of("black", "블랙", "검정"),
                        "Cream", List.of("cream", "크림", "아이보리"), "Red", List.of("red", "레드", "빨강")));
        matchOne(result, InputStep.ATTITUDE, normalized,
                Map.of("Quiet", List.of("quiet", "차분", "조용"), "Balanced", List.of("절제", "은은한 포인트"),
                        "Expressive", List.of("expressive", "대담", "강렬")));
        matchMany(result, InputStep.CONTEXTS, normalized,
                Map.of("Work", List.of("work", "출근", "업무"), "Daily", List.of("daily", "일상"),
                        "Weekend", List.of("weekend", "주말"), "Travel", List.of("travel", "여행")));
        matchOne(result, InputStep.LOCKED_ATTRIBUTE, normalized,
                Map.of("Shape", List.of("shape", "형태"), "Weight", List.of("weight", "무게", "가벼"),
                        "Color", List.of("색상은 꼭", "컬러는 꼭"), "Capacity", List.of("capacity", "수납력"),
                        "Styling", List.of("styling", "스타일링")));
        return result;
    }

    private void matchOne(Map<InputStep, List<String>> result, InputStep step, String text,
                          Map<String, List<String>> dictionary) {
        orderedEntries(step, dictionary).stream()
                .filter(entry -> entry.getValue().stream().anyMatch(text::contains))
                .findFirst()
                .ifPresent(entry -> result.put(step, List.of(entry.getKey())));
    }

    private void matchMany(Map<InputStep, List<String>> result, InputStep step, String text,
                           Map<String, List<String>> dictionary) {
        List<String> matches = orderedEntries(step, dictionary).stream()
                .filter(entry -> entry.getValue().stream().anyMatch(text::contains))
                .map(Map.Entry::getKey)
                .toList();
        if (!matches.isEmpty()) {
            result.put(step, matches);
        }
    }

    private List<String> validateAndCanonicalize(InputStep inputStep, List<String> values) {
        ChoiceStepResponse step = STEPS.stream().filter(item -> item.step() == inputStep).findFirst().orElseThrow();
        if (!step.multiple() && values.size() != 1) {
            throw new InvalidStateException(step.title() + " 단계는 하나만 선택할 수 있습니다.");
        }
        List<String> canonical = values.stream().map(value -> step.options().stream()
                .filter(option -> option.value().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new InvalidStateException("허용되지 않은 선택값입니다: " + value))
                .value()).distinct().toList();
        if (canonical.isEmpty()) {
            throw new InvalidStateException("한 개 이상의 선택값이 필요합니다.");
        }
        return canonical;
    }

    private List<Map.Entry<String, List<String>>> orderedEntries(
            InputStep step,
            Map<String, List<String>> dictionary
    ) {
        return STEPS.stream().filter(item -> item.step() == step).findFirst().orElseThrow().options().stream()
                .filter(option -> dictionary.containsKey(option.value()))
                .map(option -> Map.entry(option.value(), dictionary.get(option.value())))
                .toList();
    }

    private void apply(ExperienceSession session, InputStep step, List<String> values) {
        String first = values.get(0);
        switch (step) {
            case SILHOUETTE -> session.setSilhouette(first);
            case STRUCTURE -> session.setStructurePreference(first);
            case PROPORTION -> session.setProportion(first);
            case COLOR -> session.setColor(first);
            case ATTITUDE -> session.setAttitude(first);
            case CONTEXTS -> session.setContexts(String.join("|", values));
            case LOCKED_ATTRIBUTE -> session.setLockedAttribute(first);
        }
    }

    private boolean isMissing(ExperienceSession session, InputStep step) {
        return switch (step) {
            case SILHOUETTE -> blank(session.getSilhouette());
            case STRUCTURE -> blank(session.getStructurePreference());
            case PROPORTION -> blank(session.getProportion());
            case COLOR -> blank(session.getColor());
            case ATTITUDE -> blank(session.getAttitude());
            case CONTEXTS -> blank(session.getContexts());
            case LOCKED_ATTRIBUTE -> blank(session.getLockedAttribute());
        };
    }

    private InputProgressResponse toProgress(ExperienceSession session) {
        int completed = (int) STEPS.stream().filter(step -> !isMissing(session, step.step())).count();
        ChoiceStepResponse next = STEPS.stream().filter(step -> isMissing(session, step.step())).findFirst().orElse(null);
        int percent = Math.round(completed * 100f / STEPS.size());
        return new InputProgressResponse(
                completed, STEPS.size(), percent,
                next == null ? null : next.step(),
                next == null ? null : next.prompt(),
                completed == STEPS.size(),
                completed == STEPS.size() ? 350 : 650,
                completed == STEPS.size() ? "취향이 완성됐어요. 결과를 만들 준비가 됐습니다." : "좋아요. 다음 선택으로 천천히 이어가 볼게요.",
                session.getLastInputMode(),
                ApiMapper.toPreference(session)
        );
    }

    private InputInterpretationResponse response(
            ExperienceSession session, InputMode mode, String transcript, String source,
            Map<String, List<String>> extracted, List<String> applied
    ) {
        return new InputInterpretationResponse(mode, transcript, source, extracted, applied, toProgress(session));
    }

    private ExperienceSession findSource(ExperienceSession target, UUID sourceId) {
        if (sourceId != null) {
            ExperienceSession source = journeyService.findSession(sourceId);
            validateSource(target, source);
            return source;
        }
        return sessionRepository.findByDemoCustomerIdOrderByCreatedAtDesc(target.getDemoCustomerId()).stream()
                .filter(candidate -> !candidate.getId().equals(target.getId()))
                .filter(ExperienceSession::isDataConsent)
                .filter(candidate -> !isMissing(candidate, InputStep.SILHOUETTE))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("이어갈 수 있는 이전 취향이 없습니다."));
    }

    private void validateSource(ExperienceSession target, ExperienceSession source) {
        if (!source.getDemoCustomerId().equals(target.getDemoCustomerId()) || !source.isDataConsent()) {
            throw new InvalidStateException("같은 고객의 동의된 세션만 이어갈 수 있습니다.");
        }
    }

    private void copyPreferences(ExperienceSession source, ExperienceSession target) {
        target.setSilhouette(source.getSilhouette());
        target.setStructurePreference(source.getStructurePreference());
        target.setProportion(source.getProportion());
        target.setColor(source.getColor());
        target.setAttitude(source.getAttitude());
        target.setContexts(source.getContexts());
        target.setLockedAttribute(source.getLockedAttribute());
    }

    private void requireEditable(ExperienceSession session) {
        if (session.getStatus().ordinal() >= ExperienceStatus.RESERVED.ordinal()) {
            throw new InvalidStateException("예약 이후에는 취향 입력을 변경할 수 없습니다.");
        }
    }

    private void resetDownstream(ExperienceSession session) {
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
        session.setRevealStage(RevealStage.NOT_STARTED);
        session.setStatus(STEPS.stream().allMatch(step -> !isMissing(session, step.step()))
                ? ExperienceStatus.PREFERENCES_SAVED : ExperienceStatus.CREATED);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static ChoiceStepResponse step(
            InputStep step, String title, String prompt, boolean multiple, ChoiceOptionResponse... options
    ) {
        return new ChoiceStepResponse(step, title, prompt, multiple, List.of(options));
    }

    private static ChoiceOptionResponse option(String value, String label) {
        return new ChoiceOptionResponse(value, label);
    }
}
