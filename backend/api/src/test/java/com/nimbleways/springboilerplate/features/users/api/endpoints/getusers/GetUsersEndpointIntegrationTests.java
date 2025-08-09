package com.nimbleways.springboilerplate.features.users.api.endpoints.getusers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbleways.springboilerplate.common.domain.valueobjects.Role;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import com.nimbleways.springboilerplate.features.users.domain.usecases.suts.GetUsersSut;
import com.nimbleways.springboilerplate.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(controllers = GetUsersEndpoint.class)
@Import(GetUsersSut.class)
class GetUsersEndpointIntegrationTests extends BaseWebMvcIntegrationTests {

    private static final String GET_USERS_ENDPOINT = "/users";

    @Autowired
    private GetUsersSut sut;

    @Test
    void getting_users_with_admin_user_and_a_single_user_in_repository_returns_that_user() throws Exception {
        User admin = createUserInRepo("admin", "admin", Immutable.set.of(Role.ADMIN));

        // Act
        ResultActions resultActions = mockMvc.perform(get(GET_USERS_ENDPOINT).cookie(getAccessTokenCookie(admin)));

        resultActions
            .andExpect(status().isOk())
            .andExpect(
                jsonIgnoreArrayOrder(
                    """
                    [{"id":"%s","username":"admin","name":"admin"}]""".formatted(admin.id().toString())
                )
            );
    }

    @Test
    void getting_users_with_non_admin_user_returns_403() throws Exception {
        User user = createUserInRepo("user1", "user1", Immutable.set.of(Role.USER));

        // Act
        ResultActions resultActions = mockMvc.perform(get(GET_USERS_ENDPOINT).cookie(getAccessTokenCookie(user)));

        resultActions
            .andExpect(status().isForbidden())
            .andExpect(
                jsonIgnoreArrayOrder(
                    """
                    {"type":"about:blank","title":"errors.access_denied","status":403,
                    "detail":"errors.access_denied","instance":"/users"}"""
                )
            );
    }

    @Test
    void getting_users_with_admin_user_and_two_users_in_repository_returns_both_users() throws Exception {
        User user1 = createUserInRepo("user1", "username1", Immutable.set.of(Role.ADMIN));
        User user2 = createUserInRepo("user2", "username2", Immutable.set.of());

        // Act
        ResultActions resultActions = mockMvc.perform(get(GET_USERS_ENDPOINT).cookie(getAccessTokenCookie(user1)));

        resultActions
            .andExpect(status().isOk())
            .andExpect(
                jsonIgnoreArrayOrder(
                    """
                    [{"id":"%s","username":"username1","name":"user1"},
                    {"id":"%s","username":"username2","name":"user2"}]""".formatted(
                            user1.id().toString(),
                            user2.id().toString()
                        )
                )
            );
    }

    @Test
    void getting_users_without_accessToken_returns_401() throws Exception {
        // Act
        ResultActions resultActions = mockMvc.perform(get(GET_USERS_ENDPOINT));

        resultActions.andExpect(status().isUnauthorized());
    }

    private User createUserInRepo(String name, String username, ImmutableSet<Role> roles) {
        return sut.infra().createUser().name(name).username(username).roles(roles).execute();
    }
}
