package com.nimbleways.springboilerplate.features.users.domain.entities;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import java.time.Instant;
import java.util.UUID;
import org.eclipse.collections.api.set.ImmutableSet;

public record User(UUID id, String name, Username username, Instant createdAt, ImmutableSet<Role> roles) {}
