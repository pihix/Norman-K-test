package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeTimeProvider;
import com.nimbleways.springboilerplate.common.infra.database.entities.UserSessionDbEntity;
import com.nimbleways.springboilerplate.common.infra.database.jparepositories.JpaUserSessionRepository;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserSession;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.UserSessionRepositoryPort;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.UserSessionRepositoryPortContractTests;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import com.nimbleways.springboilerplate.testhelpers.annotations.SetupDatabase;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.junitextensions.querycount.AssertQueryCount;
import com.nimbleways.springboilerplate.testhelpers.junitextensions.querycount.Expected;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@SetupDatabase
@Import({ UserSessionRepository.class, UserRepository.class, TimeTestConfiguration.class })
@DataJpaTest
@AssertQueryCount(
    sutClass = UserSessionRepository.class,
    value = {
        @Expected(
            count = 1,
            sutActMethod = "create",
            testMethod = "creating_a_usersession_for_a_non_existing_user_throws_CannotCreateUserSessionInRepositoryException"
        ),
        @Expected(
            count = 1,
            sutActMethod = "create",
            testMethod = "creating_a_usersession_for_an_existing_user_succeed"
        ),
        @Expected(
            count = 1,
            sutActMethod = "create",
            testMethod = "creating_a_usersession_with_an_existing_refreshtoken_throws_CannotCreateUserSessionInRepositoryException"
        ),
        @Expected(
            count = 3,
            sutActMethod = "deleteUserSessionByExpirationDateBefore",
            testMethod = "deleting_expired_sessions_keeps_valid_ones"
        ),
        @Expected(
            count = 3,
            sutActMethod = "deleteUserSessionByRefreshToken",
            testMethod = "deleting_userSession_with_existing_refreshToken_succeed"
        ),
        @Expected(
            count = 1,
            sutActMethod = "deleteUserSessionByRefreshToken",
            testMethod = "deleting_userSession_with_non_existing_refreshToken_throws_RefreshTokenExpiredOrNotFoundException"
        ),
        @Expected(
            count = 1,
            sutActMethod = "findByRefreshTokenAndExpirationDateAfter",
            testMethod = "finding_by_expiration_date_does_not_return_non_existing_session"
        ),
        @Expected(
            count = 1,
            sutActMethod = "findByRefreshTokenAndExpirationDateAfter",
            testMethod = "finding_by_refreshToken_does_not_return_expired_sessions"
        ),
        @Expected(
            count = 2,
            sutActMethod = "findByRefreshTokenAndExpirationDateAfter",
            testMethod = "finding_by_refreshToken_return_valid_session"
        ),
    }
)
public class UserSessionRepositoryIntegrationTests extends UserSessionRepositoryPortContractTests {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JpaUserSessionRepository jpaUserSessionRepository;

    @Autowired
    private FakeTimeProvider timeProvider;

    @Override
    protected UserSessionRepositoryPort getUserSessionRepository() {
        return userSessionRepository;
    }

    @Override
    public UserRepositoryPort userRepository() {
        return userRepository;
    }

    @Override
    public TimeProviderPort timeProvider() {
        return timeProvider;
    }

    @Override
    protected ImmutableList<UserSession> getAllUserSessions() {
        return Immutable.collectList(jpaUserSessionRepository.findAll(), UserSessionDbEntity::toUserSession);
    }
}
