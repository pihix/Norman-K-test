package com.nimbleways.springboilerplate.features.authentication.domain.usecases.login;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;

public record LoginCommand(Username username, String password) {}
