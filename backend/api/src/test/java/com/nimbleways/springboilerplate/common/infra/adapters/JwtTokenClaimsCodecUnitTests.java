package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeTimeProvider;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.TokenClaimsCodecPortContractTests;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.AccessToken;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.configurations.JwtTokenClaimsCodecTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.utils.BeanBag;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

@UnitTest
public class JwtTokenClaimsCodecUnitTests extends TokenClaimsCodecPortContractTests {

    private final BeanBag beanBag = Instance.createBeanBag(JwtTokenClaimsCodecTestConfiguration.class);
    private final JwtTokenClaimsCodec jwtTokenClaimsCodec = beanBag.get(JwtTokenClaimsCodec.class);
    private final FakeTimeProvider timeProvider = beanBag.get(FakeTimeProvider.class);

    private static final String ISSUER = "myapp";
    private static final String SIGNING_KEY_STRING = "zdtlD3JK56m6wTTgsNFhqzjqPaaaddingFor256bits=";
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SIGNING_KEY_STRING));

    @Override
    protected JwtTokenClaimsCodec getInstance() {
        return jwtTokenClaimsCodec;
    }

    @Override
    protected FakeTimeProvider getTimeProvider() {
        return timeProvider;
    }

    @Override
    protected AccessToken getTokenWithoutRoleAttribute() {
        String jwt = getJwtBuilder().compact();
        return new AccessToken(jwt);
    }

    @Override
    protected AccessToken getTokenWithInvalidRolesArrayClaim() {
        String jwt = getJwtBuilder().claim("scope", new int[] { 1, 2, 3 }).compact();
        return new AccessToken(jwt);
    }

    @Override
    protected AccessToken getTokenWithInvalidRolesScalarClaim() {
        String jwt = getJwtBuilder().claim("scope", 10).compact();
        return new AccessToken(jwt);
    }

    @Override
    protected Instant adjustPrecision(Instant instant) {
        return instant.truncatedTo(ChronoUnit.SECONDS);
    }

    private JwtBuilder getJwtBuilder() {
        return Jwts.builder()
            .id(UUID.randomUUID().toString())
            .subject(UUID.randomUUID().toString())
            .issuer(ISSUER)
            .signWith(SIGNING_KEY)
            .issuedAt(Date.from(getTimeProvider().instant()))
            .expiration(Date.from(getTimeProvider().instant().plusSeconds(1)));
    }
}
