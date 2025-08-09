package com.nimbleways.springboilerplate.common.domain.ports;

import com.nimbleways.springboilerplate.common.domain.valueobjects.EncodedPassword;

public interface PasswordEncoderPort {
    EncodedPassword encode(String password);
    boolean matches(CharSequence plainPassword, EncodedPassword encodedPassword);
}
