package com.nimbleways.springboilerplate.features.authentication.domain.exceptions;

import com.nimbleways.springboilerplate.common.domain.exceptions.AbstractDomainException;

public abstract class AbstractAuthenticationDomainException extends AbstractDomainException {

    public AbstractAuthenticationDomainException(String message) {
        super(message);
    }

    public AbstractAuthenticationDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
