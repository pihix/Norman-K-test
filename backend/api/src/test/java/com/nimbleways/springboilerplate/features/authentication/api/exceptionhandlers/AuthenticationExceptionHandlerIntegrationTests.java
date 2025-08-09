package com.nimbleways.springboilerplate.features.authentication.api.exceptionhandlers;

import static com.nimbleways.springboilerplate.testhelpers.fixtures.SimpleFixture.aRefreshToken;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.*;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.AccessToken;
import com.nimbleways.springboilerplate.testhelpers.baseclasses.exceptionhandling.BaseExceptionHandlerIntegrationTests;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

class AuthenticationExceptionHandlerIntegrationTests extends BaseExceptionHandlerIntegrationTests {

    @Override
    protected Stream<Arguments> getExceptions() {
        return staticProvideExceptions();
    }

    private static Stream<Arguments> staticProvideExceptions() {
        return Stream.of(
            Arguments.of(
                new AccessTokenDecodingException("", new AccessToken("")),
                HttpStatus.UNAUTHORIZED,
                UNAUTHORIZED_JSON_RESPONSE
            ),
            Arguments.of(
                new BadUserCredentialException(new Username("")),
                HttpStatus.UNAUTHORIZED,
                UNAUTHORIZED_JSON_RESPONSE
            ),
            Arguments.of(
                new RefreshAndAccessTokensMismatchException(UUID.randomUUID(), UUID.randomUUID()),
                HttpStatus.UNAUTHORIZED,
                UNAUTHORIZED_JSON_RESPONSE
            ),
            Arguments.of(
                new RefreshTokenExpiredOrNotFoundException(aRefreshToken()),
                HttpStatus.UNAUTHORIZED,
                UNAUTHORIZED_JSON_RESPONSE
            ),
            Arguments.of(
                new UnknownUsernameException(new Username("")),
                HttpStatus.UNAUTHORIZED,
                UNAUTHORIZED_JSON_RESPONSE
            ),
            Arguments.of(
                new CannotCreateUserSessionInRepositoryException(
                    new Username(""),
                    new DataIntegrityViolationException("")
                ),
                HttpStatus.INTERNAL_SERVER_ERROR,
                INTERNAL_SERVER_ERROR_JSON_RESPONSE
            )
        );
    }
}
