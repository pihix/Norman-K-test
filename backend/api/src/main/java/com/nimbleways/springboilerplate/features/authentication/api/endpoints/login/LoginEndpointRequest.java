package com.nimbleways.springboilerplate.features.authentication.api.endpoints.login;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Username;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.login.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginEndpointRequest(
    @NotBlank String username,

    @NotBlank String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(new Username(username()), password());
    }
}
