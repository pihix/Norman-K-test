package com.nimbleways.springboilerplate.common.infra.adapters.fakes;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.adapters.SpringJwtDecoderContractTests;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.TokenClaims;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import com.nimbleways.springboilerplate.testhelpers.utils.BeanBag;
import com.nimbleways.springboilerplate.testhelpers.utils.Instance;
import org.springframework.security.oauth2.jwt.JwtDecoder;

@UnitTest
public final class FakeTokenClaimsCodecAsJwtDecoderUnitTests extends SpringJwtDecoderContractTests {

    private final BeanBag beanBag = Instance.createBeanBag(FakeTokenClaimsCodec.class);
    private final FakeTokenClaimsCodec fakeTokenClaimsCodec = beanBag.get(FakeTokenClaimsCodec.class);
    private final TimeProviderPort timeProvider = beanBag.get(TimeProviderPort.class);

    @Override
    protected JwtDecoder getInstance() {
        return fakeTokenClaimsCodec;
    }

    @Override
    protected String encode(TokenClaims claims) {
        return fakeTokenClaimsCodec.encode(claims).value();
    }

    @Override
    protected String getMalformedToken() {
        return "invalidToken";
    }

    @Override
    protected TimeProviderPort getTimeProvider() {
        return timeProvider;
    }
}
