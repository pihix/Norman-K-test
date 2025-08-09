package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.features.authentication.domain.ports.UserCredentialsRepositoryPort;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPortContractTests;
import com.nimbleways.springboilerplate.testhelpers.annotations.SetupDatabase;
import com.nimbleways.springboilerplate.testhelpers.junitextensions.querycount.AssertQueryCount;
import com.nimbleways.springboilerplate.testhelpers.junitextensions.querycount.Expected;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@SetupDatabase
@Import({ UserRepository.class })
@DataJpaTest
@AssertQueryCount(
    sutClass = UserRepository.class,
    value = {
        @Expected(count = 1, sutActMethod = "create", testMethod = "creating_a_new_user_succeed"),
        @Expected(count = 1, sutActMethod = "create", testMethod = "creating_a_new_user_returns_the_created_user"),
        @Expected(
            count = 1,
            sutActMethod = "create",
            testMethod = "creating_a_new_user_with_an_existing_username_throws_UserAlreadyExistsInRepositoryException"
        ),
    }
)
public class UserRepositoryIntegrationTests extends UserRepositoryPortContractTests {

    @Autowired
    private UserRepository userRepository;

    @Override
    protected UserRepositoryPort getUserRepository() {
        return userRepository;
    }

    @Override
    protected UserCredentialsRepositoryPort getUserCredentialsRepository() {
        return userRepository;
    }
}
