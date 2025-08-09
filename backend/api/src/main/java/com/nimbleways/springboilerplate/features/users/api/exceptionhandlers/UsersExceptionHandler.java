package com.nimbleways.springboilerplate.features.users.api.exceptionhandlers;

import com.nimbleways.springboilerplate.common.api.exceptionhandling.base.BaseExceptionHandler;
import com.nimbleways.springboilerplate.common.domain.ports.EventPublisherPort;
import com.nimbleways.springboilerplate.features.users.domain.exceptions.UsernameAlreadyExistsInRepositoryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class UsersExceptionHandler extends BaseExceptionHandler {

    protected UsersExceptionHandler(EventPublisherPort eventPublisher) {
        super(eventPublisher);
    }

    @ExceptionHandler({ UsernameAlreadyExistsInRepositoryException.class })
    @Nullable public final ResponseEntity<Object> handleException(
        UsernameAlreadyExistsInRepositoryException ex,
        WebRequest request
    ) {
        return getDefaultResponseEntity(
            ex,
            request,
            HttpStatus.BAD_REQUEST,
            UsersApiErrorCodes.USERNAME_ALREADY_EXISTS_ERROR
        );
    }
}
