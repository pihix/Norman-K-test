package com.nimbleways.springboilerplate.testhelpers.helpers;

import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipal;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;

public class Mapper {

    public static UserPrincipal toUserPrincipal(User user) {
        return new UserPrincipal(user.id(), user.username(), user.roles());
    }
}
