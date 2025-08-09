package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeTimeProvider;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.TokenClaims;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.configurations.JwtTokenClaimsCodecTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.utils.BeanBag;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import java.time.Instant;
import java.time.temporal.ChronoField;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@UnitTest
public final class JwtTokenClaimsCodecAsJwtDecoderUnitTests extends SpringJwtDecoderContractTests {

    private final BeanBag beanBag = Instance.createBeanBag(JwtTokenClaimsCodecTestConfiguration.class);
    private final JwtTokenClaimsCodec jwtTokenClaimsCodec = beanBag.get(JwtTokenClaimsCodec.class);
    private final FakeTimeProvider timeProvider = beanBag.get(FakeTimeProvider.class);

    @Override
    protected JwtDecoder getInstance() {
        return jwtTokenClaimsCodec;
    }

    @Override
    protected String encode(TokenClaims claims) {
        return jwtTokenClaimsCodec.encode(claims).value();
    }

    @Override
    protected String getMalformedToken() {
        return "invalidToken";
    }

    @Override
    // JWT only support time up to the seconds
    protected Instant adjustPrecision(Instant instant) {
        return instant.with(ChronoField.MILLI_OF_SECOND, 0);
    }

    @Override
    protected TimeProviderPort getTimeProvider() {
        return timeProvider;
    }
}
