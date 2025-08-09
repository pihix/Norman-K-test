package com.nimbleways.springboilerplate.features.users.domain.ports;

import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.valueobjects.NewUser;
import org.eclipse.collections.api.list.ImmutableList;

public interface UserRepositoryPort {
    User create(NewUser userToCreate);

    ImmutableList<User> findAll();
}
