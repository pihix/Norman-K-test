package com.nimbleways.springboilerplate.common.infra.adapters.fakes;

import com.nimbleways.springboilerplate.common.domain.ports.PasswordEncoderPort;
import com.nimbleways.springboilerplate.common.domain.valueobjects.EncodedPassword;

public class FakePasswordEncoder implements PasswordEncoderPort {

    @Override
    public EncodedPassword encode(String password) {
        return new EncodedPassword("$encoded$" + password);
    }

    @Override
    public boolean matches(CharSequence plainPassword, EncodedPassword encodedPassword) {
        EncodedPassword encodedPlainPassword = encode(plainPassword.toString());
        return encodedPlainPassword.equals(encodedPassword);
    }
}
