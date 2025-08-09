package com.nimbleways.springboilerplate.features.users.domain.usecases.signup;

import com.nimbleways.springboilerplate.common.domain.ports.PasswordEncoderPort;
import com.nimbleways.springboilerplate.common.domain.ports.TimeProviderPort;
import com.nimbleways.springboilerplate.common.domain.valueobjects.EncodedPassword;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.ports.UserRepositoryPort;
import com.nimbleways.springboilerplate.features.users.domain.valueobjects.NewUser;
import java.time.Instant;

public class SignupUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final TimeProviderPort timeProvider;

    public SignupUseCase(
        final UserRepositoryPort userRepository,
        final PasswordEncoderPort passwordEncoder,
        final TimeProviderPort timeProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.timeProvider = timeProvider;
    }

    public User handle(final SignupCommand signupCommand) {
        EncodedPassword encodedPassword = passwordEncoder.encode(signupCommand.plainPassword());
        Instant creationDateTime = timeProvider.instant();
        final NewUser newUser = signupCommand.toNewUser(encodedPassword, creationDateTime);
        return userRepository.create(newUser);
    }
}
