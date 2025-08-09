package com.nimbleways.springboilerplate.features.authentication.domain.usecases;

import static com.nimbleways.springboilerplate.testhelpers.CustomAssertions.assertPresent;
import static com.nimbleways.springboilerplate.testhelpers.fixtures.SimpleFixture.aLoginCommand;
import static com.nimbleways.springboilerplate.testhelpers.helpers.Mapper.toUserPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.nimbleways.springboilerplate.common.domain.events.Event;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.TokenClaims;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipal;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserSession;
import com.nimbleways.springboilerplate.features.authentication.domain.events.UserLoggedInEvent;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.BadUserCredentialException;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.UnknownUsernameException;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.login.LoginCommand;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.suts.LoginSut;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.RefreshToken;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.entities.UserBuildExecutors;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import java.time.Instant;
import java.util.Optional;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.Test;

@UnitTest
class LoginUseCaseUnitTests {

    private final LoginSut sut = Instance.create(LoginSut.class);

    @Test
    void login_with_good_username_and_password_returns_user_tokens() {
        LoginCommand loginCommand = aLoginCommand();
        User user = createUser(loginCommand).execute();

        UserPrincipal expectedUserPrincipal = toUserPrincipal(user);
        Instant expectedExpirationTime = sut
            .infra()
            .timeProvider()
            .instant()
            .plus(sut.infra().tokenProperties().accessTokenValidityDuration());

        // Act
        UserTokens tokens = sut.login(loginCommand);

        TokenClaims claims = getTokenClaims(tokens);
        assertEquals(expectedUserPrincipal, claims.userPrincipal());
        assertEquals(expectedExpirationTime, claims.expirationTime());
    }

    @Test
    void login_with_good_username_and_password_returns_new_tokens_each_time() {
        LoginCommand loginCommand = aLoginCommand();
        createUser(loginCommand).execute();

        // Act
        UserTokens firstTokens = sut.login(loginCommand);
        UserTokens secondTokens = sut.login(loginCommand);

        assertNotEquals(firstTokens.accessToken(), secondTokens.accessToken());
        assertNotEquals(firstTokens.refreshToken(), secondTokens.refreshToken());
    }

    @Test
    void login_with_good_username_and_password_publishes_UserLoggedInEvent() {
        LoginCommand loginCommand = aLoginCommand();
        User user = createUser(loginCommand).execute();

        UserPrincipal expectedUserPrincipal = toUserPrincipal(user);

        // Act
        sut.login(loginCommand);

        UserLoggedInEvent userLoggedInEvent = assertEventPublished(UserLoggedInEvent.class);
        assertEquals(expectedUserPrincipal, userLoggedInEvent.userPrincipal());
        assertNotNull(userLoggedInEvent.sourceType());
        assertEquals("Login attempt successful for user with id: " + user.id(), userLoggedInEvent.toString());
    }

    @Test
    void login_with_good_username_and_password_returns_a_refreshToken_with_expected_duration() {
        LoginCommand loginCommand = aLoginCommand();
        User user = createUser(loginCommand).execute();

        UserSession expectedUserSession = new UserSession(
            new RefreshToken("cfcd2084-95d5-35ef-a6e7-dff9f98764da"),
            sut.infra().timeProvider().instant().plus(sut.infra().tokenProperties().refreshTokenValidityDuration()),
            toUserPrincipal(user)
        );

        // Act
        sut.login(loginCommand);

        ImmutableList<UserSession> userSessions = sut.infra().userSessionRepository().findAll();
        assertThat(userSessions).containsExactly(expectedUserSession);
    }

    @Test
    void login_with_non_existing_username_throws_UnknownUsernameException() {
        LoginCommand loginCommand = aLoginCommand();

        // Act
        Exception ex = assertThrows(Exception.class, () -> sut.login(loginCommand));

        assertEquals(UnknownUsernameException.class, ex.getClass());
        assertEquals("Username not found: username", ex.getMessage());
    }

    @Test
    void login_with_good_username_and_bad_password_throws_BadUserCredentialException() {
        LoginCommand loginCommand = aLoginCommand("bad_password");
        createUser(loginCommand).plainPassword("password").execute();

        // Act
        Exception ex = assertThrows(Exception.class, () -> sut.login(loginCommand));

        assertEquals(BadUserCredentialException.class, ex.getClass());
        assertEquals("Bad password provided for username: username", ex.getMessage());
    }

    private TokenClaims getTokenClaims(UserTokens tokens) {
        return sut.infra().tokenClaimsCodec().decodeWithoutExpirationValidation(tokens.accessToken());
    }

    private UserBuildExecutors.Optionals createUser(LoginCommand loginCommand) {
        return sut
            .infra()
            .createUser()
            .username(loginCommand.username().value())
            .plainPassword(loginCommand.password());
    }

    private <T extends Event> T assertEventPublished(Class<T> eventType) {
        Optional<T> lastEvent = sut.infra().eventPublisher().lastEvent(eventType);
        return assertPresent(lastEvent);
    }
}
