package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.domain.ports.RandomGeneratorPort;
import com.nimbleways.springboilerplate.common.domain.ports.RandomGeneratorPortContractTests;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;

@UnitTest
public class RandomGeneratorUnitTests extends RandomGeneratorPortContractTests {

    @Override
    protected RandomGeneratorPort getInstance() {
        return new RandomGenerator();
    }
}
