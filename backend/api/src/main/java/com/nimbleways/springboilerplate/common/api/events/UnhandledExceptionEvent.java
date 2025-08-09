package com.nimbleways.springboilerplate.common.api.events;

import com.nimbleways.springboilerplate.common.domain.events.Event;

public record UnhandledExceptionEvent(Exception exception) implements Event {
    @Override
    public Class<?> sourceType() {
        try {
            StackTraceElement[] stackTraceElements = exception.getStackTrace();
            String className = stackTraceElements[0].getClassName();
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            return UnhandledExceptionEvent.class;
        }
    }
}
