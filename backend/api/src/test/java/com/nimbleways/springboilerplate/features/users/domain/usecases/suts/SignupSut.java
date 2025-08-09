package com.nimbleways.springboilerplate.features.users.domain.usecases.suts;

import com.nimbleways.springboilerplate.common.domain.ports.PasswordEncoderPort;
import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakePasswordEncoder;
import com.nimbleways.springboilerplate.common.infra.adapters.fakes.FakeUserRepository;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.usecases.signup.SignupCommand;
import com.nimbleways.springboilerplate.features.users.domain.usecases.signup.SignupUseCase;
import com.nimbleways.springboilerplate.testhelpers.configurations.TimeTestConfiguration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;

@Import({ SignupUseCase.class, SignupSut.Infra.class })
@RequiredArgsConstructor
public class SignupSut {

    private final SignupUseCase useCase;

    @Getter
    private final Infra infra;

    public User signup(final SignupCommand signupCommand) {
        return useCase.handle(signupCommand);
    }

    @Import({ FakeUserRepository.class, TimeTestConfiguration.class, FakePasswordEncoder.class })
    @RequiredArgsConstructor
    @Getter
    public static class Infra {

        private final FakeUserRepository userRepository;
        private final TimeProviderPort timeProvider;
        private final PasswordEncoderPort passwordEncoder;
    }
}
