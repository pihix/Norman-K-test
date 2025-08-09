package com.nimbleways.springboilerplate.testhelpers.fixtures;

import static java.util.Objects.requireNonNullElse;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipal;
import jakarta.annotation.Nullable;
import java.util.UUID;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jilt.Builder;

public class UserPrincipalFixture {

    private static final String DEFAULT_USERNAME = "username";
    private static final ImmutableSet<Role> DEFAULT_ROLES = Immutable.set.of();

    @Builder(factoryMethod = "aUserPrincipal")
    public static UserPrincipal buildUserPrincipal(
        @Nullable UUID id,
        @Nullable String username,
        @Nullable ImmutableSet<Role> roles
    ) {
        UUID idValue = requireNonNullElse(id, UUID.randomUUID());
        String usernameValue = requireNonNullElse(username, DEFAULT_USERNAME);
        ImmutableSet<Role> rolesValue = requireNonNullElse(roles, DEFAULT_ROLES);

        return new UserPrincipal(idValue, new Username(usernameValue), rolesValue);
    }
}
