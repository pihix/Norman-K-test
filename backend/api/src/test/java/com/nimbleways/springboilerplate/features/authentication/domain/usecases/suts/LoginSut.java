package com.nimbleways.springboilerplate.features.authentication.domain.usecases.suts;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.*;
import com.nimbleways.springboilerplate.features.authentication.domain.properties.TokenProperties;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.helpers.UserCreationHelpers;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.login.LoginCommand;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.login.LoginUseCase;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;
import com.nimbleways.springboilerplate.testhelpers.configurations.PropertiesTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;

@Import({ LoginUseCase.class, LoginSut.Infra.class })
@RequiredArgsConstructor
public class LoginSut {

    private final LoginUseCase useCase;

    @Getter
    private final Infra infra;

    public UserTokens login(final LoginCommand loginCommand) {
        return useCase.handle(loginCommand);
    }

    @Import(
        {
            FakeUserSessionRepository.class,
            FakeUserRepository.class,
            FakeTokenClaimsCodec.class,
            PropertiesTestConfiguration.class,
            TimeTestConfiguration.class,
            FakeEventPublisher.class,
            FakePasswordEncoder.class,
            FakeRandomGenerator.class,
        }
    )
    @RequiredArgsConstructor
    @Getter
    public static class Infra implements UserCreationHelpers {

        public final FakeUserRepository userRepository;
        public final FakeTokenClaimsCodec tokenClaimsCodec;
        public final TokenProperties tokenProperties;
        public final TimeProviderPort timeProvider;
        public final FakeEventPublisher eventPublisher;
        public final FakeUserSessionRepository userSessionRepository;
    }
}
