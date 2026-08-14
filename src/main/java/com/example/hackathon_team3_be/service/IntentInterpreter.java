package com.example.hackathon_team3_be.service;

import com.example.hackathon_team3_be.domain.ExperienceSession;

public interface IntentInterpreter {

    IntentResult interpret(ExperienceSession session);

    record IntentResult(
            String purpose,
            String priority,
            String style,
            String signature,
            String concern,
            String summary
    ) {
    }
}
