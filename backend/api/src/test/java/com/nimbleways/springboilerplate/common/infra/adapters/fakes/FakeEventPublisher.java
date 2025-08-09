package com.nimbleways.springboilerplate.common.infra.adapters.fakes;

import com.nimbleways.springboilerplate.common.domain.events.Event;
import com.nimbleways.springboilerplate.common.domain.ports.EventPublisherPort;
import com.nimbleways.springboilerplate.common.utils.collections.Mutable;
import java.util.Optional;
import org.eclipse.collections.api.list.MutableList;

public class FakeEventPublisher implements EventPublisherPort {

    private final MutableList<Event> events = Mutable.list.empty();

    @Override
    public void publishEvent(Event event) {
        events.add(event);
    }

    public <T extends Event> Optional<T> lastEvent(Class<T> eventType) {
        for (int i = events.size() - 1; i >= 0; i--) {
            Event event = events.get(i);
            if (eventType.isInstance(event)) {
                return Optional.of(eventType.cast(event));
            }
        }
        return Optional.empty();
    }
}
