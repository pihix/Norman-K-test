package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import com.nimbleways.springboilerplate.common.infra.database.entities.UserDbEntity;
import com.nimbleways.springboilerplate.common.infra.database.jparepositories.JpaUserRepository;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserCredential;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.UserCredentialsRepositoryPort;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.exceptions.UsernameAlreadyExistsInRepositoryException;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import com.nimbleways.springboilerplate.features.users.domain.valueobjects.NewUser;
import java.util.Optional;
import org.eclipse.collections.api.list.ImmutableList;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
public class UserRepository implements UserRepositoryPort, UserCredentialsRepositoryPort {

    private final JpaUserRepository jpaUserRepository;

    public UserRepository(final JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public User create(NewUser userToCreate) {
        UserDbEntity userDbEntity = UserDbEntity.from(userToCreate);
        UserDbEntity savedUserDbEntity;
        try {
            savedUserDbEntity = jpaUserRepository.saveAndFlush(userDbEntity);
        } catch (DataIntegrityViolationException ex) {
            throw new UsernameAlreadyExistsInRepositoryException(userToCreate.username(), ex);
        }
        return savedUserDbEntity.toUser();
    }

    @Override
    public ImmutableList<User> findAll() {
        return Immutable.collectList(jpaUserRepository.findAll(), UserDbEntity::toUser);
    }

    @Override
    // TODO: Retrieve from the database only the fields needed for UserCredential
    public Optional<UserCredential> findUserCredentialByUsername(Username username) {
        return jpaUserRepository.findByUsername(username.value()).map(UserDbEntity::toUserCredential);
    }
}
