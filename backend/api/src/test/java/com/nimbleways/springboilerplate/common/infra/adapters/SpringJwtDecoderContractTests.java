package com.nimbleways.springboilerplate.common.infra.adapters;

import static com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipalBuilder.aUserPrincipal;
import static org.assertj.core.api.Assertions.assertThatObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.TokenClaims;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipal;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

public abstract class SpringJwtDecoderContractTests {

    private TimeProviderPort timeProvider;
    private JwtDecoder jwtDecoder;

    @BeforeEach
    public void createSut() {
        timeProvider = getTimeProvider();
        jwtDecoder = getInstance();
    }

    @Test
    void returns_valid_Jwt_with_one_role() {
        TokenClaims claims = getTokenClaims(timeProvider.instant().plusSeconds(10), Role.USER);
        String token = encode(claims);

        Jwt jwt = jwtDecoder.decode(token);

        assertEquals(adjustPrecision(claims.expirationTime()), jwt.getExpiresAt());
        assertEquals(adjustPrecision(claims.creationTime()), jwt.getIssuedAt());
        assertEquals(List.of("USER"), jwt.getClaim("scope"));
        assertEquals(getExpectedSubject(claims.userPrincipal()), jwt.getSubject());
    }

    @Test
    void returns_valid_Jwt_without_role() {
        TokenClaims claims = getTokenClaims(timeProvider.instant().plusSeconds(10));
        String token = encode(claims);

        Jwt jwt = jwtDecoder.decode(token);

        assertEquals(List.of(), jwt.getClaim("scope"));
    }

    @Test
    void returns_valid_Jwt_with_two_roles() {
        TokenClaims claims = getTokenClaims(timeProvider.instant().plusSeconds(1), Role.USER, Role.ADMIN);
        String token = encode(claims);

        Jwt jwt = jwtDecoder.decode(token);

        assertThatObject(jwt.getClaim("scope"))
            .asInstanceOf(InstanceOfAssertFactories.iterable(String.class))
            .containsExactlyInAnyOrder("USER", "ADMIN");
    }

    @Test
    void throws_JwtValidationException_if_expired() {
        TokenClaims claims = getTokenClaims(timeProvider.instant().minusSeconds(1), Role.USER);
        String token = encode(claims);

        JwtValidationException exception = assertThrows(JwtValidationException.class, () -> jwtDecoder.decode(token));

        assertEquals(List.of(OAuth2Error.class), exception.getErrors().stream().map(OAuth2Error::getClass).toList());
    }

    @Test
    void throws_BadJwtException_if_malformed() {
        String token = getMalformedToken();

        Exception exception = assertThrows(Exception.class, () -> jwtDecoder.decode(token));

        assertEquals(BadJwtException.class, exception.getClass());
    }

    private String getExpectedSubject(UserPrincipal userPrincipal) {
        return "%s,%s".formatted(userPrincipal.id(), userPrincipal.username().value());
    }

    @NotNull private TokenClaims getTokenClaims(Instant expirationTime, Role... roles) {
        return new TokenClaims(
            aUserPrincipal().roles(Immutable.set.of(roles)).build(),
            expirationTime.minusSeconds(1),
            expirationTime
        );
    }

    // --------------------------------- Protected Methods ------------------------------- //
    protected abstract JwtDecoder getInstance();

    protected abstract String encode(TokenClaims claims);

    protected abstract String getMalformedToken();

    protected Instant adjustPrecision(Instant instant) {
        return instant;
    }

    protected abstract TimeProviderPort getTimeProvider();
}
