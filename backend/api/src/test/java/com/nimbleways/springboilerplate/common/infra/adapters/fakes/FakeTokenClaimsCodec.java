package com.nimbleways.springboilerplate.common.infra.adapters.fakes;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fasterxml.jackson.datatype.eclipsecollections.EclipseCollectionsModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.mappers.RoleMapper;
import com.nimbleways.springboilerplate.common.utils.collections.Mutable;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.TokenClaims;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipal;
import com.nimbleways.springboilerplate.features.authentication.domain.exceptions.AccessTokenDecodingException;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.TokenClaimsCodecPort;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.AccessToken;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

@Import(TimeTestConfiguration.class)
public class FakeTokenClaimsCodec implements TokenClaimsCodecPort, JwtDecoder {

    private static final ObjectMapper objectMapper = createObjectMapper();
    private final TimeProviderPort timeProvider;
    private AccessToken lastCreatedToken;

    public FakeTokenClaimsCodec(TimeProviderPort timeProvider) {
        this.timeProvider = timeProvider;
    }

    @SneakyThrows
    @Override
    public AccessToken encode(TokenClaims tokenClaims) {
        ClaimWrapper claimWrapper = new ClaimWrapper(UUID.randomUUID(), tokenClaims);
        String tokenJson = objectMapper.writeValueAsString(claimWrapper);
        lastCreatedToken = new AccessToken(tokenJson);
        return lastCreatedToken;
    }

    @SneakyThrows
    @Override
    public TokenClaims decodeWithoutExpirationValidation(AccessToken token) {
        ClaimWrapper claimWrapper;
        try {
            claimWrapper = objectMapper.readValue(token.value(), ClaimWrapper.class);
        } catch (MismatchedInputException | JsonParseException exception) {
            throw new AccessTokenDecodingException(exception, token);
        }
        if (claimWrapper.tokenClaims.userPrincipal().roles() == null) {
            throw new AccessTokenDecodingException("missing claim 'scope'", token);
        }
        return claimWrapper.tokenClaims();
    }

    public AccessToken lastCreatedToken() {
        return lastCreatedToken;
    }

    @Override
    public Jwt decode(String token) {
        TokenClaims tokenClaims;
        try {
            tokenClaims = decodeWithoutExpirationValidation(new AccessToken(token));
        } catch (AccessTokenDecodingException e) {
            throw new BadJwtException("Cannot parse JWT", e);
        }
        if (tokenClaims.expirationTime().isBefore(timeProvider.instant())) {
            throw new JwtValidationException("", List.of(new OAuth2Error("invalid_token")));
        }

        UserPrincipal user = tokenClaims.userPrincipal();
        List<String> roles = Mutable.collectList(user.roles(), RoleMapper.INSTANCE::fromValueObject);
        String subject = "%s,%s".formatted(user.id(), user.username().value());
        return Jwt.withTokenValue(token)
            .header("alg", "none")
            .claim("scope", roles)
            .expiresAt(tokenClaims.expirationTime())
            .issuedAt(tokenClaims.creationTime())
            .subject(subject)
            .build();
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new EclipseCollectionsModule());

        objectMapper
            .coercionConfigFor(LogicalType.Textual)
            .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
            .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail);

        return objectMapper;
    }

    private record ClaimWrapper(UUID rand, TokenClaims tokenClaims) {}
}
