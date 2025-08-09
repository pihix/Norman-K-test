package com.nimbleways.springboilerplate.common.infra.adapters.fakes;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserSession;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.UserSessionRepositoryPortContractTests;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.utils.BeanBag;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import org.eclipse.collections.api.list.ImmutableList;

@UnitTest
public class FakeUserSessionRepositoryUnitTests extends UserSessionRepositoryPortContractTests {

    private final BeanBag beans = Instance.createBeanBag(
        FakeUserRepository.class,
        FakeUserSessionRepository.class,
        TimeTestConfiguration.class
    );

    @Override
    protected FakeUserSessionRepository getUserSessionRepository() {
        return beans.get(FakeUserSessionRepository.class);
    }

    @Override
    public FakeUserRepository userRepository() {
        return beans.get(FakeUserRepository.class);
    }

    @Override
    public TimeProviderPort timeProvider() {
        return beans.get(FakeTimeProvider.class);
    }

    @Override
    protected ImmutableList<UserSession> getAllUserSessions() {
        return getUserSessionRepository().findAll();
    }
}
