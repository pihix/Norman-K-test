package com.nimbleways.springboilerplate.features.authentication.domain.entities;

import com.nimbleways.springboilerplate.common.domain.valueobjects.EncodedPassword;

public record UserCredential(UserPrincipal userPrincipal, EncodedPassword encodedPassword) {}
