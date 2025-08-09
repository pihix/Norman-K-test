package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.domain.ports.RandomGeneratorPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RandomGenerator implements RandomGeneratorPort {

    @Override
    public UUID uuid() {
        return UUID.randomUUID();
    }
}
