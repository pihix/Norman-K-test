package com.nimbleways.springboilerplate.features.users.domain.valueobjects;

import com.nimbleways.springboilerplate.common.domain.valueobjects.EncodedPassword;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import java.time.Instant;
import org.eclipse.collections.api.set.ImmutableSet;

public record NewUser(
    String name,
    Username username,
    EncodedPassword encodedPassword,
    Instant creationDateTime,
    ImmutableSet<Role> roles
) {}
