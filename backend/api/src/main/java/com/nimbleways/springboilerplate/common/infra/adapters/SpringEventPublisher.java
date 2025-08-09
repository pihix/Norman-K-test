package com.nimbleways.springboilerplate.common.infra.adapters;

import com.nimbleways.springboilerplate.common.domain.events.Event;
import com.nimbleways.springboilerplate.common.domain.ports.EventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventPublisher implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publishEvent(Event event) {
        applicationEventPublisher.publishEvent(event);
    }
}
