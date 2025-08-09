package com.nimbleways.springboilerplate.features.users.api.endpoints.getusers;

import com.nimbleways.springboilerplate.features.users.api.endpoints.getusers.GetUsersEndpointResponse.Item;
import com.nimbleways.springboilerplate.features.users.domain.entities.User;
import java.util.ArrayList;
import org.eclipse.collections.api.list.ImmutableList;

public final class GetUsersEndpointResponse extends ArrayList<Item> {

    public record Item(String id, String name, String username) {}

    public static GetUsersEndpointResponse from(ImmutableList<User> users) {
        GetUsersEndpointResponse getUsersEndpointResponse = new GetUsersEndpointResponse(users.size());
        for (User user : users) {
            getUsersEndpointResponse.add(from(user));
        }
        return getUsersEndpointResponse;
    }

    private GetUsersEndpointResponse(int initialCapacity) {
        super(initialCapacity);
    }

    private static Item from(User user) {
        return new Item(user.id().toString(), user.name(), user.username().value());
    }
}
