package com.nimbleways.springboilerplate.features.authentication.domain.entities;

import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.RefreshToken;
import java.time.Instant;

public record UserSession(RefreshToken refreshToken, Instant expirationDate, UserPrincipal userPrincipal) {}
