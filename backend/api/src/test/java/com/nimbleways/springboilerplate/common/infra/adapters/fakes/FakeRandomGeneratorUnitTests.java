package com.nimbleways.springboilerplate.common.infra.adapters.fakes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbleways.springboilerplate.common.domain.ports.RandomGeneratorPort;
import com.nimbleways.springboilerplate.common.domain.ports.RandomGeneratorPortContractTests;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class FakeRandomGeneratorUnitTests extends RandomGeneratorPortContractTests {

    @Override
    protected RandomGeneratorPort getInstance() {
        return new FakeRandomGenerator();
    }

    @Test
    void FakeRandomGenerator_is_deterministic() {
        FakeRandomGenerator fakeRandomGenerator1 = new FakeRandomGenerator();
        FakeRandomGenerator fakeRandomGenerator2 = new FakeRandomGenerator();
        int count = 10_000;
        for (int i = 0; i < count; ++i) {
            assertEquals(fakeRandomGenerator1.uuid(), fakeRandomGenerator2.uuid());
        }
    }
}
