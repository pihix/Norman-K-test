package com.nimbleways.springboilerplate.features.authentication.domain.usecases.helpers;

import static com.nimbleways.springboilerplate.testhelpers.fixtures.NewUserFixture.buildNewUser;

import com.nimbleways.springboilerplate.common.domain.ports.PasswordEncoderPort;
import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.entities.UserBuildExecutor;
import com.nimbleways.springboilerplate.features.users.domain.entities.UserBuildExecutors;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import com.nimbleways.springboilerplate.features.users.domain.valueobjects.NewUser;
import com.nimbleways.springboilerplate.testhelpers.annotations.ExecutableBuilder;
import jakarta.annotation.Nullable;
import org.eclipse.collections.api.set.ImmutableSet;

public interface UserCreationHelpers {
    UserRepositoryPort userRepository();
    TimeProviderPort timeProvider();

    default UserBuildExecutors.Optionals createUser() {
        return UserBuildExecutor.start().timeProvider(timeProvider()).userRepository(userRepository());
    }

    class ExecutableBuilders {

        @ExecutableBuilder
        public static User createUser(
            @Nullable String name,
            @Nullable String username,
            @Nullable String plainPassword,
            @Nullable ImmutableSet<Role> roles,
            @Nullable PasswordEncoderPort passwordEncoder,
            TimeProviderPort timeProvider,
            UserRepositoryPort userRepository
        ) {
            NewUser newUser = buildNewUser(name, username, plainPassword, roles, timeProvider, passwordEncoder);
            return userRepository.create(newUser);
        }
    }
}
