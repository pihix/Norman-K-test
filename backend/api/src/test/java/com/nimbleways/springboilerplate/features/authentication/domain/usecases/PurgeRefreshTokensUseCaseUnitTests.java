package com.nimbleways.springboilerplate.features.authentication.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserSession;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.suts.PurgeRefreshTokensSut;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

@UnitTest
class PurgeRefreshTokensUseCaseUnitTests {

    private final PurgeRefreshTokensSut sut = Instance.create(PurgeRefreshTokensSut.class);

    @Test
    void purging_deletes_all_expired_refreshTokens() {
        User user = createUser();
        Instant start = sut.infra().timeProvider().instant();
        addSession(user, start);
        addSession(user, start.plusSeconds(1));
        UserSession nonExpiredUserSession = addSession(user, start.plusSeconds(3));

        sut.infra().timeProvider().moveTime(Duration.ofSeconds(2));

        // Act
        sut.purgeRefreshToken();

        assertEquals(List.of(nonExpiredUserSession), sut.infra().userSessionRepository().findAll());
    }

    private User createUser() {
        return sut.infra().createUser().execute();
    }

    private UserSession addSession(User user, Instant expirationDate) {
        return sut.infra().createUserSession(user).expirationDate(expirationDate).execute();
    }
}
