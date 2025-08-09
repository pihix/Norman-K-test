package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.api.beans.InfraBeanConfiguration;
import com.nimbleways.springboilerplate.common.domain.ports.PasswordEncoderPort;
import com.nimbleways.springboilerplate.common.domain.ports.PasswordEncoderPortContractTests;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;

@UnitTest
public class SpringSecurityPasswordEncoderUnitTests extends PasswordEncoderPortContractTests {

    private static final PasswordEncoderPort INSTANCE = new SpringSecurityPasswordEncoder(
        new InfraBeanConfiguration().passwordEncoder()
    );

    @Override
    protected PasswordEncoderPort getInstance() {
        return INSTANCE;
    }
}
