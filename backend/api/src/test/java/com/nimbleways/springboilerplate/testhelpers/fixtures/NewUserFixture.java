package com.nimbleways.springboilerplate.testhelpers.fixtures;

import static java.util.Objects.requireNonNullElse;

import com.nimbleways.springboilerplate.common.domain.ports.PasswordEncoderPort;
import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakePasswordEncoder;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.features.users.domain.valueobjects.NewUser;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import jakarta.annotation.Nullable;
import java.util.UUID;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jilt.Builder;

public class NewUserFixture {

    private static final ImmutableSet<Role> DEFAULT_ROLES = Immutable.set.of();
    private static final FakePasswordEncoder DEFAULT_PASSWORD_ENCODER = new FakePasswordEncoder();
    private static final TimeProviderPort DEFAULT_TIME_PROVIDER = TimeTestConfiguration.fakeTimeProvider();

    @Builder(factoryMethod = "aNewUser")
    public static NewUser buildNewUser(
        @Nullable String name,
        @Nullable String username,
        @Nullable String plainPassword,
        @Nullable ImmutableSet<Role> roles,
        @Nullable TimeProviderPort timeProvider,
        @Nullable PasswordEncoderPort passwordEncoder
    ) {
        UUID id = UUID.randomUUID();
        String nameValue = requireNonNullElse(name, "name-" + id);
        String usernameValue = requireNonNullElse(username, "username-" + id);
        ImmutableSet<Role> rolesValue = requireNonNullElse(roles, DEFAULT_ROLES);
        PasswordEncoderPort passwordEncoderValue = requireNonNullElse(passwordEncoder, DEFAULT_PASSWORD_ENCODER);
        String plainPasswordValue = requireNonNullElse(plainPassword, "password-" + id);
        TimeProviderPort timeProviderValue = requireNonNullElse(timeProvider, DEFAULT_TIME_PROVIDER);

        return new NewUser(
            nameValue,
            new Username(usernameValue),
            passwordEncoderValue.encode(plainPasswordValue),
            timeProviderValue.instant(),
            rolesValue
        );
    }
}
