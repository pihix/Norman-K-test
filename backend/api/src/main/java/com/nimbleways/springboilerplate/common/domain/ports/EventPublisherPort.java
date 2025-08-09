package com.nimbleways.springboilerplate.common.domain.ports;

import com.nimbleways.springboilerplate.common.domain.events.Event;

public interface EventPublisherPort {
    void publishEvent(Event event);
}
