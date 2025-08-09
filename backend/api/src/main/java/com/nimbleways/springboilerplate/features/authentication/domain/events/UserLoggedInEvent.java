package com.nimbleways.springboilerplate.features.authentication.domain.events;

import com.nimbleways.springboilerplate.common.domain.events.Event;
import com.nimbleways.springboilerplate.features.authentication.domain.entities.UserPrincipal;

public record UserLoggedInEvent(UserPrincipal userPrincipal, Class<?> sourceType) implements Event {
    @Override
    public String toString() {
        return "Login attempt successful for user with id: %s".formatted(userPrincipal.id());
    }
}
