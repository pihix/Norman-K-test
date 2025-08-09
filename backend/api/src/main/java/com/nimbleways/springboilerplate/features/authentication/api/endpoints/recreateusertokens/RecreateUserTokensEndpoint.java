package com.nimbleways.springboilerplate.features.authentication.api.endpoints.recreateusertokens;

import com.nimbleways.springboilerplate.features.authentication.api.services.AuthenticationResponseEntity;
import com.nimbleways.springboilerplate.features.authentication.domain.properties.TokenProperties;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.recreateusertokens.RecreateUserTokensCommand;
import com.nimbleways.springboilerplate.features.authentication.domain.usecases.recreateusertokens.RecreateUserTokensUseCase;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.AccessToken;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.RefreshToken;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecreateUserTokensEndpoint {

    public static final String URL = "/auth/refreshToken";

    private final AuthenticationResponseEntity authenticationResponseEntity;
    private final RecreateUserTokensUseCase recreateUserTokensUseCase;

    public RecreateUserTokensEndpoint(
        final RecreateUserTokensUseCase recreateUserTokensUseCase,
        @Value("${server.servlet.context-path}") final String apiContextPath,
        final TokenProperties tokenProperties
    ) {
        this.authenticationResponseEntity = new AuthenticationResponseEntity(apiContextPath, tokenProperties);
        this.recreateUserTokensUseCase = recreateUserTokensUseCase;
    }

    @PostMapping(URL)
    @PreAuthorize("permitAll()")
    public ResponseEntity<Void> refreshToken(
        @CookieValue(
            name = AuthenticationResponseEntity.ACCESS_TOKEN_COOKIE_NAME,
            required = false
        ) final String accessToken,
        @CookieValue(
            name = AuthenticationResponseEntity.REFRESH_TOKEN_COOKIE_NAME,
            required = false
        ) final String refreshToken
    ) {
        RecreateUserTokensCommand command = createCommand(refreshToken, accessToken);
        UserTokens userTokens = recreateUserTokensUseCase.handle(command);
        return authenticationResponseEntity.from(userTokens);
    }

    @NotNull private static RecreateUserTokensCommand createCommand(String refreshToken, String accessToken) {
        return new RecreateUserTokensCommand(
            new UserTokens(new AccessToken(accessToken), new RefreshToken(refreshToken))
        );
    }
}
