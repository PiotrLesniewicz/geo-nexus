package com.geo.survey.domain.service;

import com.geo.survey.domain.exception.BusinessRuleViolationException;
import com.geo.survey.math.engine.LevelingEngine;
import com.geo.survey.math.value.LevelingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LevelingStrategy {
    private final List<LevelingEngine> engines;

    public LevelingEngine getEngine(LevelingType type) {
        return engines.stream()
                .filter(e -> e.getType() == type)
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "No engine found for leveling type: [%s]".formatted(type)));
    }
}
