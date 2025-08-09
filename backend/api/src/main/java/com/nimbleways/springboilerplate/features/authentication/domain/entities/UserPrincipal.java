package com.nimbleways.springboilerplate.features.authentication.domain.entities;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import java.util.UUID;
import org.eclipse.collections.api.set.ImmutableSet;

public record UserPrincipal(
    UUID id,
    Username username,

    ImmutableSet<Role> roles
) {}
