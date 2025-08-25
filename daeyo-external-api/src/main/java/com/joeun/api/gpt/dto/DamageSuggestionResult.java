package com.joeun.api.gpt.dto;

import java.util.List;

public record DamageSuggestionResult(
        double damageRate,            // 0.0 ~ 1.0
        String verdict,               // NO_DAMAGE | MINOR | MODERATE | SEVERE
        List<String> observations,
        String suggestedAction
) {}
