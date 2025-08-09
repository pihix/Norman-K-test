package com.nimbleways.springboilerplate.common.api;

import static com.nimbleways.springboilerplate.testhelpers.CustomAssertions.assertPresent;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.nimbleways.springboilerplate.common.api.events.UnhandledExceptionEvent;
import com.nimbleways.springboilerplate.common.domain.events.Event;
import com.nimbleways.springboilerplate.common.domain.ports.EventPublisherPort;
import com.nimbleways.springboilerplate.common.infra.adapters.SpringEventPublisher;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.ILoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.SubstituteLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(
    {
        LoggingEventListener.class, // SUT
        SpringEventPublisher.class,
    }
)
class LoggingEventListenerIntegrationTests {

    @Autowired
    private EventPublisherPort eventPublisher;

    @Autowired
    private InMemoryLogs inMemoryLogs; // used by ILoggerFactory

    @TestConfiguration
    static class TestConfig {

        @Bean
        public static InMemoryLogs inMemoryLogs() {
            return new InMemoryLogs();
        }

        @Bean
        public static ILoggerFactory loggerFactory(InMemoryLogs inMemoryLogs) {
            return s -> new SubstituteLogger(s, inMemoryLogs.eventQueue, false);
        }
    }

    @BeforeEach
    public void clearLogs() {
        inMemoryLogs.clear();
    }

    @Test
    void publishing_an_exception_will_log_the_exception_once_with_error_level() {
        UnhandledExceptionEvent event = new UnhandledExceptionEvent(new Exception("This is an exception"));

        // Act
        eventPublisher.publishEvent(event);

        assertEquals(1, inMemoryLogs.size());
        Optional<LoggingEvent> logEvent = inMemoryLogs.getLoggingEvent("java.lang.Exception: This is an exception");
        LoggingEvent loggingEvent = assertPresent(logEvent);
        assertEquals(LoggingEventListenerIntegrationTests.class.getName(), loggingEvent.getLoggerName());
        assertEquals(event.exception(), loggingEvent.getThrowable());
        assertEquals(Level.ERROR, loggingEvent.getLevel());
    }

    @Test
    void publishing_a_domain_event_will_log_the_event_once_with_info_level() {
        FakeEvent event = new FakeEvent();

        // Act
        eventPublisher.publishEvent(event);

        assertEquals(1, inMemoryLogs.size());
        Optional<LoggingEvent> logEvent = inMemoryLogs.getLoggingEvent("This is a FakeEvent");
        LoggingEvent loggingEvent = assertPresent(logEvent);
        assertEquals(LoggingEventListenerIntegrationTests.class.getName(), loggingEvent.getLoggerName());
        assertEquals(Level.INFO, loggingEvent.getLevel());
    }

    @Test
    void publishing_an_exception_with_unknown_thrower_will_log_the_exception_with_UnhandledExceptionEvent_as_source() {
        Exception exception = getExceptionWithInvalidStack();
        UnhandledExceptionEvent event = new UnhandledExceptionEvent(exception);

        // Act
        eventPublisher.publishEvent(event);

        assertEquals(1, inMemoryLogs.size());
        Optional<LoggingEvent> logEvent = inMemoryLogs.getLoggingEvent(exception.toString());
        LoggingEvent loggingEvent = assertPresent(logEvent);
        assertEquals(UnhandledExceptionEvent.class.getName(), loggingEvent.getLoggerName());
        assertEquals(event.exception(), loggingEvent.getThrowable());
        assertEquals(Level.ERROR, loggingEvent.getLevel());
    }

    private static Exception getExceptionWithInvalidStack() {
        Exception exception = new Exception("This is an exception");
        StackTraceElement invalidStackTrace = new StackTraceElement("InvalidClassName", "methodName", "fileName", 1);
        exception.setStackTrace(new StackTraceElement[] { invalidStackTrace });
        return exception;
    }

    static class InMemoryLogs {

        private final ConcurrentLinkedQueue<SubstituteLoggingEvent> eventQueue;

        public InMemoryLogs() {
            this.eventQueue = new ConcurrentLinkedQueue<>();
        }

        public int size() {
            return eventQueue.size();
        }

        public Optional<LoggingEvent> getLoggingEvent(String message) {
            return eventQueue.stream().filter(log -> log.getMessage().equals(message)).findFirst().map(e -> e);
        }

        public void clear() {
            eventQueue.clear();
        }
    }

    private record FakeEvent() implements Event {
        @Override
        public String toString() {
            return "This is a FakeEvent";
        }

        @Override
        public Class<?> sourceType() {
            return LoggingEventListenerIntegrationTests.class;
        }
    }
}
