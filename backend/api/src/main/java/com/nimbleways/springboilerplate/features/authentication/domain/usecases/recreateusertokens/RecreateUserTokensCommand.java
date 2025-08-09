package com.nimbleways.springboilerplate.features.authentication.domain.usecases.recreateusertokens;

import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;

public record RecreateUserTokensCommand(UserTokens previousUserToken) {}
