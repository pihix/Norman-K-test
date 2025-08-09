package com.nimbleways.springboilerplate.common.domain.ports;

import java.time.Instant;

public interface TimeProviderPort {
    Instant instant();
}
