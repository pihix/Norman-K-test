package com.nimbleways.springboilerplate.features.authentication.domain.properties;

import java.time.Duration;

public record TokenProperties(
    boolean https,
    Duration accessTokenValidityDuration,
    Duration refreshTokenValidityDuration
) {}
