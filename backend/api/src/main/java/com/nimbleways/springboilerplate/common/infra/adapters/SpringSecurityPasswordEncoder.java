package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.domain.ports.PasswordEncoderPort;
import com.nimbleways.springboilerplate.common.domain.valueobjects.EncodedPassword;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class SpringSecurityPasswordEncoder implements PasswordEncoderPort {

    private final PasswordEncoder passwordEncoder;

    public SpringSecurityPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public EncodedPassword encode(String password) {
        return new EncodedPassword(passwordEncoder.encode(password));
    }

    @Override
    public boolean matches(CharSequence plainPassword, EncodedPassword encodedPassword) {
        return passwordEncoder.matches(plainPassword, encodedPassword.value());
    }
}
