package com.nimbleways.springboilerplate.features.users.api.exceptionhandlers;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import com.nimbleways.springboilerplate.features.users.domain.exceptions.UsernameAlreadyExistsInRepositoryException;
import com.nimbleways.springboilerplate.testhelpers.baseclasses.exceptionhandling.BaseExceptionHandlerIntegrationTests;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

class UsersExceptionHandlerIntegrationTests extends BaseExceptionHandlerIntegrationTests {

    @Override
    protected Stream<Arguments> getExceptions() {
        return staticProvideExceptions();
    }

    private static Stream<Arguments> staticProvideExceptions() {
        return Stream.of(
            Arguments.of(
                new UsernameAlreadyExistsInRepositoryException(
                    new Username(""),
                    new DataIntegrityViolationException("")
                ),
                HttpStatus.BAD_REQUEST,
                """
                {"type":"about:blank","title":"errors.username_already_exists","status":400,
                "detail":"errors.username_already_exists","instance":"/exception-handling/throw"}"""
            )
        );
    }
}
