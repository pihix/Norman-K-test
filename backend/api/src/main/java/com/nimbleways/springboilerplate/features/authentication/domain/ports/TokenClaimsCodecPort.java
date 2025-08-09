package com.nimbleways.springboilerplate.features.authentication.domain.ports;

import com.nimbleways.springboilerplate.features.authentication.domain.entities.TokenClaims;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.AccessToken;

public interface TokenClaimsCodecPort {
    AccessToken encode(TokenClaims tokenClaims);
    TokenClaims decodeWithoutExpirationValidation(AccessToken token);
}
