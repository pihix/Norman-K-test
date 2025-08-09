package com.nimbleways.springboilerplate.common.api.beans;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.datatype.eclipsecollections.EclipseCollectionsModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return jacksonObjectMapperBuilder ->
            jacksonObjectMapperBuilder
                .modulesToInstall(EclipseCollectionsModule.class)
                .postConfigurer(objectMapper -> objectMapper.setDefaultLeniency(false))
                .featuresToEnable(
                    DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES,
                    DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS,
                    DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY,
                    DeserializationFeature.FAIL_ON_TRAILING_TOKENS,
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
                )
                .featuresToDisable(
                    DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
                    MapperFeature.ALLOW_COERCION_OF_SCALARS,
                    DeserializationFeature.ACCEPT_FLOAT_AS_INT
                );
    }
}
