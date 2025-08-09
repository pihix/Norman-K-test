package com.nimbleways.springboilerplate.features.authentication.domain.ports;

import static com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipalBuilder.aUserPrincipal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeTimeProvider;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.TokenClaims;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.AccessTokenDecodingException;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.AccessToken;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class TokenClaimsCodecPortContractTests {

    private TokenClaimsCodecPort tokenCodec;

    @BeforeEach
    public void createSut() {
        tokenCodec = getInstance();
    }

    @Test
    void encoding_a_valid_claim_returns_a_non_null_accesstoken() {
        TokenClaims claims = getTokenClaims(Role.USER);

        // Act
        AccessToken token = tokenCodec.encode(claims);

        assertNotNull(token);
    }

    @Test
    void encoding_the_same_claim_twice_returns_different_accesstokens() {
        TokenClaims claims = getTokenClaims(Role.USER);
        AccessToken firstToken = tokenCodec.encode(claims);

        // Act
        AccessToken secondToken = tokenCodec.encode(claims);

        assertNotEquals(firstToken, secondToken);
    }

    @Test
    void decoding_a_valid_accesstoken_with_one_role_returns_the_initial_claim() {
        TokenClaims claims = getTokenClaims(Role.USER);
        AccessToken token = tokenCodec.encode(claims);

        // Act
        TokenClaims decodedClaims = tokenCodec.decodeWithoutExpirationValidation(token);

        assertEquals(claims.userPrincipal(), decodedClaims.userPrincipal());
        assertEquals(adjustPrecision(claims.creationTime()), decodedClaims.creationTime());
        assertEquals(adjustPrecision(claims.expirationTime()), decodedClaims.expirationTime());
    }

    @Test
    void decoding_a_valid_accesstoken_with_two_roles_returns_the_initial_claim() {
        TokenClaims claims = getTokenClaims(Role.USER, Role.ADMIN);
        AccessToken token = tokenCodec.encode(claims);

        // Act
        TokenClaims decodedClaims = tokenCodec.decodeWithoutExpirationValidation(token);

        assertThat(decodedClaims.userPrincipal().roles()).containsExactlyInAnyOrder(Role.USER, Role.ADMIN);
    }

    @Test
    void decoding_a_valid_accesstoken_with_empty_roles_returns_the_initial_claim() {
        TokenClaims claims = getTokenClaims();
        AccessToken token = tokenCodec.encode(claims);

        // Act
        TokenClaims decodedClaims = tokenCodec.decodeWithoutExpirationValidation(token);

        assertEquals(0, decodedClaims.userPrincipal().roles().size());
    }

    @Test
    void decoding_an_expired_accesstoken_returns_the_initial_claim() {
        Instant epochZero = Instant.ofEpochMilli(0);
        TokenClaims claims = new TokenClaims(aUserPrincipal().build(), epochZero, epochZero.plusSeconds(1));
        AccessToken token = tokenCodec.encode(claims);

        // Act
        TokenClaims decodedClaims = tokenCodec.decodeWithoutExpirationValidation(token);

        assertEquals(claims, decodedClaims);
    }

    @Test
    void decoding_an_malformed_accesstoken_throws_AccessTokenDecodingException() {
        AccessToken token = new AccessToken("abc");

        // Act
        Exception exception = assertThrows(Exception.class, () -> tokenCodec.decodeWithoutExpirationValidation(token));

        assertEquals(AccessTokenDecodingException.class, exception.getClass());
        assertEquals("Cannot decode token 'abc'", exception.getMessage());
    }

    @Test
    void decoding_an_accesstoken_without_roles_throws_AccessTokenDecodingException() {
        AccessToken token = getTokenWithoutRoleAttribute();

        // Act
        Exception exception = assertThrows(Exception.class, () -> tokenCodec.decodeWithoutExpirationValidation(token));

        assertEquals(AccessTokenDecodingException.class, exception.getClass());
        assertTrue(exception.getMessage().startsWith("Cannot decode token '" + token.value() + "'"));
    }

    @Test
    void decoding_an_accesstoken_with_invalid_roles_array_claim_throws_AccessTokenDecodingException() {
        AccessToken token = getTokenWithInvalidRolesArrayClaim();

        // Act
        Exception exception = assertThrows(Exception.class, () -> tokenCodec.decodeWithoutExpirationValidation(token));

        assertEquals(AccessTokenDecodingException.class, exception.getClass());
        assertTrue(exception.getMessage().startsWith("Cannot decode token '" + token.value() + "'"));
    }

    @Test
    void decoding_an_accesstoken_with_invalid_roles_scalar_claim_throws_AccessTokenDecodingException() {
        AccessToken token = getTokenWithInvalidRolesScalarClaim();

        // Act
        Exception exception = assertThrows(Exception.class, () -> tokenCodec.decodeWithoutExpirationValidation(token));

        assertEquals(AccessTokenDecodingException.class, exception.getClass());
        assertTrue(exception.getMessage().startsWith("Cannot decode token '" + token.value() + "'"));
    }

    @NotNull private TokenClaims getTokenClaims(Role... roles) {
        Instant now = getTimeProvider().instant();
        return new TokenClaims(aUserPrincipal().roles(Immutable.set.of(roles)).build(), now, now.plusSeconds(1));
    }

    // --------------------------------- Protected Methods ------------------------------- //
    protected abstract TokenClaimsCodecPort getInstance();

    protected abstract FakeTimeProvider getTimeProvider();

    protected abstract AccessToken getTokenWithoutRoleAttribute();

    protected abstract AccessToken getTokenWithInvalidRolesArrayClaim();

    protected abstract AccessToken getTokenWithInvalidRolesScalarClaim();

    protected Instant adjustPrecision(Instant instant) {
        return instant;
    }
}
