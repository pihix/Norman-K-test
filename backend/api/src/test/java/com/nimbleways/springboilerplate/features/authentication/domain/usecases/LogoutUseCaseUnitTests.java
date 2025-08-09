package com.nimbleways.springboilerplate.features.authentication.domain.usecases;

import static com.nimbleways.springboilerplate.testhelpers.fixtures.SimpleFixture.aRefreshToken;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserSession;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.RefreshTokenExpiredOrNotFoundException;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.suts.LogoutSut;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.RefreshToken;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import java.util.List;
import org.junit.jupiter.api.Test;

@UnitTest
class LogoutUseCaseUnitTests {

    private final LogoutSut sut = Instance.create(LogoutSut.class);

    @Test
    void logout_with_existing_refreshToken_deletes_session_from_repository() {
        UserSession userSession1 = sut.infra().loginWithNewUser().session();
        UserSession userSession2 = sut.infra().loginWithNewUser().session();

        // Act
        sut.logout(userSession1.refreshToken());

        assertEquals(List.of(userSession2), sut.infra().userSessionRepository().findAll());
    }

    @Test
    void logout_with_non_existing_refreshToken_throws_RefreshTokenExpiredOrNotFoundException() {
        RefreshToken nonExistingRefreshToken = aRefreshToken();

        // Act
        Exception ex = assertThrows(Exception.class, () -> sut.logout(nonExistingRefreshToken));

        assertEquals(RefreshTokenExpiredOrNotFoundException.class, ex.getClass());
        assertEquals(
            "RefreshToken has expired or not present in database: " + nonExistingRefreshToken.value(),
            ex.getMessage()
        );
    }
}
