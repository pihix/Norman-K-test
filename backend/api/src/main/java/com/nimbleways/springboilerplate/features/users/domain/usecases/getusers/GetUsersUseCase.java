package com.nimbleways.springboilerplate.features.users.domain.usecases.getusers;

import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import org.eclipse.collections.api.list.ImmutableList;

public class GetUsersUseCase {

    private final UserRepositoryPort userRepository;

    public GetUsersUseCase(final UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public ImmutableList<User> handle() {
        return userRepository.findAll();
    }
}
