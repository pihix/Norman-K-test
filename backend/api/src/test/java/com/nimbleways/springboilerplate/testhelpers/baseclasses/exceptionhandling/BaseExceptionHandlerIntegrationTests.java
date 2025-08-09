package com.nimbleways.springboilerplate.testhelpers.baseclasses.exceptionhandling;

import static com.nimbleways.springboilerplate.Application.BASE_PACKAGE_NAME;
import static com.nimbleways.springboilerplate.testhelpers.baseclasses.exceptionhandling.ExceptionHandlingFakeEndpoint.EXCEPTION_GET_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nimbleways.springboilerplate.common.api.events.UnhandledExceptionEvent;
import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.common.utils.collections.Mutable;
import com.nimbleways.springboilerplate.testhelpers.baseclasses.BaseWebMvcIntegrationTests;
import com.nimbleways.springboilerplate.testhelpers.utils.ClassFinder;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ExceptionHandlingFakeEndpoint.class)
@Import({ BaseExceptionHandlerIntegrationTests.InMemoryEventListener.class })
public abstract class BaseExceptionHandlerIntegrationTests extends BaseWebMvcIntegrationTests {

    @Autowired
    private ExceptionHandlingFakeEndpoint fakeEndpoint;

    @Autowired
    private InMemoryEventListener inMemoryEventListener;

    protected static final String UNAUTHORIZED_JSON_RESPONSE =
        """
        {"type":"about:blank","title":"errors.unauthorized","status":401,
        "detail":"errors.unauthorized","instance":"/exception-handling/throw"}""";

    protected static final String INTERNAL_SERVER_ERROR_JSON_RESPONSE =
        """
        {"type":"about:blank","title":"errors.internal_server_error","status":500,
        "detail":"errors.internal_server_error","instance":"/exception-handling/throw"}""";

    @ParameterizedTest
    @MethodSource("staticProvideExceptions")
    void get_on_a_endpoint_that_throws_returns_expected_problemDetails_with_expected_http_code(
        Exception exceptionToThrow,
        HttpStatus expectedHttpStatus,
        String expectedJsonBody
    ) throws Exception {
        fakeEndpoint.exceptionToThrow(exceptionToThrow);

        // Act
        ResultActions resultActions = mockMvc.perform(get(EXCEPTION_GET_ENDPOINT));

        resultActions
            .andExpect(status().is(expectedHttpStatus.value()))
            .andExpect(jsonIgnoreArrayOrder(expectedJsonBody));
        ImmutableList<UnhandledExceptionEvent> events = inMemoryEventListener.getEvents();
        assertEquals(1, events.size());
        assertEquals(exceptionToThrow, events.get(0).exception());
    }

    @Test
    void staticProvideExceptions_returns_exactly_all_custom_exceptions_under_parent_package() {
        ImmutableList<? extends Class<?>> definedExceptions = ClassFinder.findAllNonAbstractExceptions(
            ClassFinder.getParentPackage(getClass())
        );
        List<? extends Class<?>> testedExceptions = getExceptions()
            .map(arg -> arg.get()[0].getClass())
            // Ignore third party exceptions
            .filter(c -> c.getPackageName().startsWith(BASE_PACKAGE_NAME))
            .toList();

        // Act
        MutableList<? extends Class<?>> untestedExceptions = definedExceptions.toList();
        untestedExceptions.removeAll(testedExceptions);
        MutableList<? extends Class<?>> extraExceptions = Mutable.list.ofAll(testedExceptions);
        extraExceptions.removeAllIterable(definedExceptions);

        assertEquals(
            List.of(),
            untestedExceptions,
            "all custom exceptions must be declared in 'staticProvideExceptions()'"
        );
        assertEquals(
            List.of(),
            extraExceptions,
            "all exceptions returned by staticProvideExceptions() must be defined in the parent package"
        );
    }

    // Implementation of this method must always return staticProvideExceptions().
    // This is needed because staticProvideExceptions must be static (junit constraint)
    // and static method cannot be abstract.
    protected abstract Stream<Arguments> getExceptions();

    static class InMemoryEventListener {

        private final ConcurrentLinkedQueue<UnhandledExceptionEvent> eventQueue = new ConcurrentLinkedQueue<>();

        @EventListener
        public void logEvent(UnhandledExceptionEvent event) {
            eventQueue.add(event);
        }

        public ImmutableList<UnhandledExceptionEvent> getEvents() {
            return Immutable.list.ofAll(eventQueue);
        }

        public void clear() {
            eventQueue.clear();
        }
    }
}
