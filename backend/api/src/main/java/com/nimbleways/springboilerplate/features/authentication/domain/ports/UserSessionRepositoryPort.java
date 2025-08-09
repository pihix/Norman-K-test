package com.nimbleways.springboilerplate.features.authentication.domain.ports;

import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserSession;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.RefreshToken;
import java.time.Instant;
import java.util.Optional;

public interface UserSessionRepositoryPort {
    void create(UserSession userSession);

    Optional<UserSession> findByRefreshTokenAndExpirationDateAfter(RefreshToken refreshToken, Instant now);

    void deleteUserSessionByRefreshToken(RefreshToken refreshToken);

    void deleteUserSessionByExpirationDateBefore(Instant instant);
}
