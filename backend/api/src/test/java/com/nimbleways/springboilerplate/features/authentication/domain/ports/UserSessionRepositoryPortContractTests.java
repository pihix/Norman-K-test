package com.nimbleways.springboilerplate.features.authentication.domain.ports;

import static com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipalBuilder.aUserPrincipal;
import static com.nimbleways.springboilerplate.testhelpers.CustomAssertions.assertPresent;
import static com.nimbleways.springboilerplate.testhelpers.fixtures.SimpleFixture.aRefreshToken;
import static com.nimbleways.springboilerplate.testhelpers.helpers.Mapper.toUserPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.adapters.TimeProvider;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserSession;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.CannotCreateUserSessionInRepositoryException;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.RefreshTokenExpiredOrNotFoundException;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.helpers.UserCreationHelpers;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.RefreshToken;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.eclipse.collections.api.list.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Transactional
public abstract class UserSessionRepositoryPortContractTests implements UserCreationHelpers {

    private UserSessionRepositoryPort userSessionRepository;
    private static final TimeProviderPort timeProvider = TimeProvider.UTC;

    @BeforeEach
    public void createSut() {
        userSessionRepository = getUserSessionRepository();
    }

    @Override
    public abstract UserRepositoryPort userRepository();

    @Override
    public abstract TimeProviderPort timeProvider();

    @Test
    void creating_a_usersession_for_a_non_existing_user_throws_CannotCreateUserSessionInRepositoryException() {
        UserSession userSession = new UserSession(aRefreshToken(), timeProvider.instant(), aUserPrincipal().build());

        // Act
        Exception exception = assertThrows(Exception.class, () -> userSessionRepository.create(userSession));

        assertEquals(CannotCreateUserSessionInRepositoryException.class, exception.getClass());
        assertEquals("Cannot create UserSession in repository for user 'username'", exception.getMessage());
    }

    @Test
    void creating_a_usersession_for_an_existing_user_succeed() {
        User user = createUser().execute();
        UserSession userSession = newUserSession(user, timeProvider.instant());

        // Act
        userSessionRepository.create(userSession);

        assertEquals(List.of(userSession), getAllUserSessions());
    }

    @Test
    void creating_a_usersession_with_an_existing_refreshtoken_throws_CannotCreateUserSessionInRepositoryException() {
        User user = createUser().username("username").execute();
        UserSession userSession = newUserSession(user, timeProvider.instant());
        userSessionRepository.create(userSession);

        // Act
        Exception exception = assertThrows(Exception.class, () -> userSessionRepository.create(userSession));

        assertEquals(CannotCreateUserSessionInRepositoryException.class, exception.getClass());
        assertEquals("Cannot create UserSession in repository for user 'username'", exception.getMessage());
    }

    @Test
    void deleting_userSession_with_existing_refreshToken_succeed() {
        User user = createUser().execute();
        UserSession userSession = newUserSession(user, timeProvider.instant());
        userSessionRepository.create(userSession);

        // Act
        userSessionRepository.deleteUserSessionByRefreshToken(userSession.refreshToken());

        assertEquals(0, getAllUserSessions().size());
    }

    @Test
    void deleting_userSession_with_non_existing_refreshToken_throws_RefreshTokenExpiredOrNotFoundException() {
        RefreshToken randomRefreshToken = aRefreshToken();

        // Act
        Exception exception = assertThrows(Exception.class, () ->
            userSessionRepository.deleteUserSessionByRefreshToken(randomRefreshToken)
        );

        assertEquals(RefreshTokenExpiredOrNotFoundException.class, exception.getClass());
    }

    @Test
    void finding_by_refreshToken_does_not_return_expired_sessions() {
        User user = createUser().execute();
        UserSession userSession = newUserSession(user, timeProvider.instant());
        userSessionRepository.create(userSession);

        // Act
        Optional<UserSession> insertedUserSession = userSessionRepository.findByRefreshTokenAndExpirationDateAfter(
            userSession.refreshToken(),
            userSession.expirationDate().plusMillis(1)
        );

        assertTrue(insertedUserSession.isEmpty());
    }

    @Test
    void finding_by_refreshToken_return_valid_session() {
        User user = createUser().execute();
        UserSession userSession = newUserSession(user, timeProvider.instant());
        userSessionRepository.create(userSession);

        // Act
        Optional<UserSession> insertedUserSessionOptional =
            userSessionRepository.findByRefreshTokenAndExpirationDateAfter(
                userSession.refreshToken(),
                userSession.expirationDate()
            );

        UserSession insertedUserSession = assertPresent(insertedUserSessionOptional);
        assertEquals(userSession, insertedUserSession);
    }

    @Test
    void finding_by_expiration_date_does_not_return_non_existing_session() {
        // Act
        Optional<UserSession> insertedUserSession = userSessionRepository.findByRefreshTokenAndExpirationDateAfter(
            aRefreshToken(),
            timeProvider.instant()
        );

        assertTrue(insertedUserSession.isEmpty());
    }

    @Test
    void deleting_expired_sessions_keeps_valid_ones() {
        User user = createUser().execute();
        Instant now = timeProvider.instant();
        UserSession expiredSession = newUserSession(user, now.minusMillis(1));
        UserSession firstValidSession = newUserSession(user, now);
        UserSession secondValidSession = newUserSession(user, now.plusMillis(1));
        userSessionRepository.create(expiredSession);
        userSessionRepository.create(firstValidSession);
        userSessionRepository.create(secondValidSession);

        // Act
        userSessionRepository.deleteUserSessionByExpirationDateBefore(now);

        ImmutableList<UserSession> allUserSessions = getAllUserSessions();
        assertThat(allUserSessions).containsExactlyInAnyOrder(firstValidSession, secondValidSession);
    }

    @NotNull private UserSession newUserSession(User user, Instant expirationDate) {
        return new UserSession(aRefreshToken(), expirationDate, toUserPrincipal(user));
    }

    // --------------------------------- Protected Methods ------------------------------- //
    protected abstract UserSessionRepositoryPort getUserSessionRepository();

    protected abstract ImmutableList<UserSession> getAllUserSessions();
}
