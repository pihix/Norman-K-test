package com.nimbleways.springboilerplate.features.authentication.domain.usecases.suts;

import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeTokenClaimsCodec;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeUserRepository;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeUserSessionRepository;
import com.nimbleways.springboilerplate.features.authentication.domain.ports.TokenClaimsCodecPort;
import com.nimbleways.springboilerplate.features.authentication.domain.properties.TokenProperties;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.helpers.LoginHelpers;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.logout.LogoutUseCase;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.RefreshToken;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import com.nimbleways.springboilerplate.testhelpers.configurations.PropertiesTestConfiguration;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;

@Import({ LogoutUseCase.class, LogoutSut.Infra.class })
@RequiredArgsConstructor
public class LogoutSut {

    private final LogoutUseCase useCase;

    @Getter
    private final Infra infra;

    public void logout(final RefreshToken refreshToken) {
        useCase.handle(refreshToken);
    }

    @Import(
        {
            FakeUserSessionRepository.class,
            FakeUserRepository.class,
            FakeTokenClaimsCodec.class,
            PropertiesTestConfiguration.class,
            TimeTestConfiguration.class,
        }
    )
    @RequiredArgsConstructor
    @Getter
    public static class Infra implements LoginHelpers {

        private final UserRepositoryPort userRepository;
        private final TimeProviderPort timeProvider;
        private final TokenClaimsCodecPort tokenClaimsCodec;
        private final TokenProperties tokenProperties;
        private final FakeUserSessionRepository userSessionRepository;
    }
}
