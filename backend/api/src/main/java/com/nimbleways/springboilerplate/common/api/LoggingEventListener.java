package com.nimbleways.springboilerplate.common.api;

import com.nimbleways.springboilerplate.common.api.events.UnhandledExceptionEvent;
import com.nimbleways.springboilerplate.common.domain.events.Event;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoggingEventListener {

    private final ILoggerFactory loggerFactory;

    @EventListener
    public void logEvent(Event event) {
        Logger logger = loggerFactory.getLogger(event.sourceType().getName());
        if (event instanceof UnhandledExceptionEvent unhandledExceptionEvent) {
            Exception exception = unhandledExceptionEvent.exception();
            logger.error(Objects.toString(exception), exception);
        } else {
            logger.info(event.toString());
        }
    }
}
