package com.nimbleways.springboilerplate.features.authentication.domain.usecases.helpers;

import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserSession;
import com.nimbleways.springboilerplate.features.authentication.domain.valueobjects.UserTokens;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;

public interface LoginHelpers extends UserCreationHelpers, UserTokensHelpers, UserSessionHelpers {
    default LoginData loginWithNewUser() {
        User user = createUser().execute();
        UserTokens userTokens = newUserTokens(user);
        UserSession userSession = createUserSession(user).refreshToken(userTokens.refreshToken()).execute();
        return new LoginData(user, userTokens, userSession);
    }

    record LoginData(User user, UserTokens tokens, UserSession session) {}
}
