package com.nimbleways.springboilerplate.testhelpers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

public class CustomAssertions {

    public static <T> T assertPresent(Optional<T> optional) {
        assertTrue(optional.isPresent());
        return optional.orElseThrow();
    }
}
