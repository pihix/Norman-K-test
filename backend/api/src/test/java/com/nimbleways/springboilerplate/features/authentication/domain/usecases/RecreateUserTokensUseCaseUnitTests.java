package com.nimbleways.springboilerplate.features.authentication.domain.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.RefreshAndAccessTokensMismatchException;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.RefreshTokenExpiredOrNotFoundException;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.helpers.LoginHelpers;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.recreateusertokens.RecreateUserTokensCommand;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.suts.RecreateUserTokensSut;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import org.junit.jupiter.api.Test;

@UnitTest
class RecreateUserTokensUseCaseUnitTests {

    private final RecreateUserTokensSut sut = Instance.create(RecreateUserTokensSut.class);

    @Test
    void recreating_tokens_returns_new_user_tokens_and_update_user_sessions() {
        UserTokens oldUserTokens = sut.infra().loginWithNewUser().tokens();

        // Act
        UserTokens newUserTokens = sut.recreateUserTokens(new RecreateUserTokensCommand(oldUserTokens));

        assertNotEquals(oldUserTokens.accessToken(), newUserTokens.accessToken());
        assertNotEquals(oldUserTokens.refreshToken(), newUserTokens.refreshToken());
        assertEquals(sut.infra().tokenClaimsCodec().lastCreatedToken(), newUserTokens.accessToken());
        assertThat(sut.infra().userSessionRepository().findAll())
            .hasSize(1)
            .allMatch(s -> s.refreshToken().equals(newUserTokens.refreshToken()));
    }

    @Test
    void recreating_tokens_with_non_existing_refreshToken_throws_RefreshTokenExpiredOrNotFoundException() {
        UserTokens userTokens = createUserTokens();

        // Act
        Exception ex = assertThrows(Exception.class, () ->
            sut.recreateUserTokens(new RecreateUserTokensCommand(userTokens))
        );

        assertEquals(RefreshTokenExpiredOrNotFoundException.class, ex.getClass());
        assertEquals(
            "RefreshToken has expired or not present in database: " + userTokens.refreshToken().value(),
            ex.getMessage()
        );
    }

    @Test
    void recreating_tokens_with_accessToken_and_refreshToken_belonging_to_different_users_throws_RefreshAndAccessTokensMismatchException() {
        LoginHelpers.LoginData user1LoginData = sut.infra().loginWithNewUser();
        LoginHelpers.LoginData user2LoginData = sut.infra().loginWithNewUser();
        UserTokens mismatchedTokens = new UserTokens(
            user1LoginData.tokens().accessToken(),
            user2LoginData.tokens().refreshToken()
        );

        // Act
        Exception ex = assertThrows(Exception.class, () ->
            sut.recreateUserTokens(new RecreateUserTokensCommand(mismatchedTokens))
        );

        assertEquals(RefreshAndAccessTokensMismatchException.class, ex.getClass());
        assertEquals(
            "User ids from RefreshToken and AccessToken don't match. RefreshToken userId: %s | AccessToken userId: %s".formatted(
                    user2LoginData.user().id(),
                    user1LoginData.user().id()
                ),
            ex.getMessage()
        );
    }

    private UserTokens createUserTokens() {
        User user = sut.infra().createUser().execute();
        return sut.infra().newUserTokens(user);
    }
}
