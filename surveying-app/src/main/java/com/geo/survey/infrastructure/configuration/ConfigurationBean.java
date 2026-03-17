package com.geo.survey.infrastructure.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.geo.survey.math.engine.LevelingEngine;
import com.geo.survey.math.engine.OneWayDoubleLeveling;
import com.geo.survey.math.engine.OneWayLeveling;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class ConfigurationBean {

    @Bean
    public Clock clock() {
        return Clock.tickSeconds(ZoneId.systemDefault());
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public LevelingEngine oneWayLeveling() {
        return new OneWayLeveling();
    }

    @Bean
    public LevelingEngine oneWayDoubleLeveling() {
        return new OneWayDoubleLeveling();
    }
}
