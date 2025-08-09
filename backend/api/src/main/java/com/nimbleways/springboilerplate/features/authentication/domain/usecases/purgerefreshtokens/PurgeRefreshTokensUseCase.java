package com.nimbleways.springboilerplate.features.authentication.domain.usecases.purgerefreshtokens;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.UserSessionRepositoryPort;

public class PurgeRefreshTokensUseCase {

    private final UserSessionRepositoryPort userSessionRepository;
    private final TimeProviderPort timeProvider;

    public PurgeRefreshTokensUseCase(UserSessionRepositoryPort userSessionRepository, TimeProviderPort timeProvider) {
        this.userSessionRepository = userSessionRepository;
        this.timeProvider = timeProvider;
    }

    public void handle() {
        userSessionRepository.deleteUserSessionByExpirationDateBefore(timeProvider.instant());
    }
}
