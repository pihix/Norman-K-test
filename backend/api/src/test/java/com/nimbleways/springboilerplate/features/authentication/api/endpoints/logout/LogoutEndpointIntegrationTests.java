package com.nimbleways.springboilerplate.features.authentication.api.endpoints.logout;

import static com.nimbleways.springboilerplate.testhelpers.helpers.TokenHelpers.urlEncodeAccessToken;
import static com.nimbleways.springboilerplate.testhelpers.helpers.TokenHelpers.urlEncodeRefreshToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbleways.springboilerplate.features.authentication.domain.usecases.suts.LogoutSut;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;
import com.nimbleways.springboilerplate.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = LogoutEndpoint.class)
@Import(LogoutSut.class)
class LogoutEndpointIntegrationTests extends BaseWebMvcIntegrationTests {

    private static final String LOGOUT_ENDPOINT = "/auth/logout";

    @Autowired
    private LogoutSut sut;

    @Test
    void returns_expired_cookies() throws Exception {
        UserTokens userTokens = sut.infra().loginWithNewUser().tokens();

        // Act
        ResultActions resultActions = mockMvc.perform(
            post(LOGOUT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", urlEncodeAccessToken(userTokens)))
                .cookie(new Cookie("refreshToken", urlEncodeRefreshToken(userTokens)))
        );

        resultActions
            .andExpect(status().isOk())
            .andExpect(cookie().maxAge("accessToken", 0))
            .andExpect(cookie().maxAge("refreshToken", 0))
            .andExpect(noContent());
    }

    @Test
    void returns_forbidden_when_not_authenticated() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(post(LOGOUT_ENDPOINT).contentType(MediaType.APPLICATION_JSON));

        resultActions
            .andExpect(status().isUnauthorized())
            .andExpect(
                jsonIgnoreArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.unauthorized","status":401,
                    "detail":"errors.unauthorized","instance":"/auth/logout"}"""
                )
            );
    }
}
