package com.nimbleways.springboilerplate.features.users.domain.exceptions;

import com.nimbleways.springboilerplate.common.domain.exceptions.AbstractDomainException;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;

public class UsernameAlreadyExistsInRepositoryException extends AbstractDomainException {

    public UsernameAlreadyExistsInRepositoryException(Username username, Throwable cause) {
        super("Username '%s' already exist in repository".formatted(username.value()), cause);
    }
}
