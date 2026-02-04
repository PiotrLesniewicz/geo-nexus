package com.geo.survey.math.engine;

import com.geo.survey.math.value.LevelingObservation;
import com.geo.survey.math.value.LevelingResultReport;
import com.geo.survey.math.value.LevelingType;

import java.util.List;

public interface LevelingEngine {

    LevelingType getType();

    LevelingResultReport calculate(Double startH, Double endH, List<LevelingObservation> data);
}