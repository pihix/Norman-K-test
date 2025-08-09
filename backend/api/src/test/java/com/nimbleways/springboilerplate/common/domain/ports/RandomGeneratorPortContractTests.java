package com.nimbleways.springboilerplate.common.domain.ports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public abstract class RandomGeneratorPortContractTests {

    @Test
    void generating_a_huge_number_of_random_uuid_returns_0_duplicates() {
        RandomGeneratorPort randomGenerator = getInstance();
        int count = 10_000;
        Set<UUID> uuids = new HashSet<>(count);
        for (int i = 0; i < count; ++i) {
            uuids.add(randomGenerator.uuid());
        }
        assertEquals(count, uuids.size());
    }

    // --------------------------------- Protected Methods ------------------------------- //
    protected abstract RandomGeneratorPort getInstance();
}
