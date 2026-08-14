package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.domain.ExperienceSession;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class RuleBasedIntentInterpreter implements IntentInterpreter {

    @Override
    public IntentResult interpret(ExperienceSession session) {
        String contexts = session.getContexts().replace('|', ' ');
        String normalizedContext = contexts.toLowerCase(Locale.ROOT);
        String normalizedAttitude = session.getAttitude().toLowerCase(Locale.ROOT);

        String priority = normalizedContext.contains("travel") || normalizedContext.contains("여행")
                ? "Mobility / Lightweight"
                : "Daily Versatility / Comfort";
        String style = normalizedAttitude.contains("quiet") || normalizedAttitude.contains("차분")
                ? "Relaxed / Modern"
                : "Expressive / Contemporary";
        String signature = session.getStructurePreference() + " Structure / " + session.getColor();
        String concern = session.getLockedAttribute().toLowerCase(Locale.ROOT).contains("shape")
                || session.getLockedAttribute().contains("형태")
                ? "Styling Versatility"
                : "Weight and Practicality";
        String purpose = contexts.replace("  ", " + ").replace(" ", " + ");
        String summary = "%s 환경을 오가며 사용할 수 있고, %s를 우선하면서 %s의 인상을 유지하는 %s 가방을 원합니다."
                .formatted(contexts, priority, style, signature);
        return new IntentResult(purpose, priority, style, signature, concern, summary);
    }
}
