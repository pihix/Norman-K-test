package com.nimbleways.springboilerplate.features.users.api.endpoints.signup;

import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.usecases.signup.SignupCommand;
import com.nimbleways.springboilerplate.features.users.domain.usecases.signup.SignupUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SignupEndpoint {

    public static final String URL = "/auth/signup";

    private final SignupUseCase signupUseCase;

    public SignupEndpoint(SignupUseCase signupUseCase) {
        this.signupUseCase = signupUseCase;
    }

    @PostMapping(URL)
    @PreAuthorize("permitAll()")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupEndpointResponse createUser(@RequestBody @Valid final SignupEndpointRequest signupEndpointRequest) {
        final SignupCommand signupCommand = signupEndpointRequest.toSignupCommand();
        final User user = signupUseCase.handle(signupCommand);
        return SignupEndpointResponse.from(user);
    }
}
