package com.nimbleways.springboilerplate.common.infra.adapters.fakes;

import com.nimbleways.springboilerplate.features.authentication.domain.ports.UserCredentialsRepositoryPort;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPortContractTests;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;

@UnitTest
public class FakeUserRepositoryUnitTests extends UserRepositoryPortContractTests {

    private final FakeUserRepository fakeUserRepository;

    public FakeUserRepositoryUnitTests() {
        super();
        fakeUserRepository = Instance.create(FakeUserRepository.class);
    }

    @Override
    protected UserRepositoryPort getUserRepository() {
        return fakeUserRepository;
    }

    @Override
    protected UserCredentialsRepositoryPort getUserCredentialsRepository() {
        return fakeUserRepository;
    }
}
