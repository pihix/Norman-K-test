package com.nimbleways.springboilerplate.common.api.exceptionhandling.base;

import static org.checkerframework.checker.nullness.util.NullnessUtil.castNonNull;

import com.nimbleways.springboilerplate.common.api.events.UnhandledExceptionEvent;
import com.nimbleways.springboilerplate.common.domain.ports.EventPublisherPort;
import java.net.URI;
import java.util.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Order(0)
public abstract class BaseResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final ErrorAttributes ERROR_ATTRIBUTES_PROVIDER = new DefaultErrorAttributes();
    private final EventPublisherPort eventPublisher;

    protected BaseResponseEntityExceptionHandler(EventPublisherPort eventPublisher) {
        super();
        this.eventPublisher = eventPublisher;
    }

    @Nullable protected ResponseEntity<Object> getDefaultResponseEntity(
        Exception ex,
        WebRequest request,
        HttpStatusCode status,
        ErrorCode errorCode
    ) {
        ProblemDetail body = this.createProblemDetail(ex, status, errorCode.code(), null, null, request);
        body.setTitle(errorCode.code());
        return this.handleExceptionInternal(ex, body, new HttpHeaders(), status, request);
    }

    @Override
    @Nullable protected ResponseEntity<Object> handleExceptionInternal(
        @NotNull Exception ex,
        @Nullable Object body,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode statusCode,
        @NotNull WebRequest request
    ) {
        eventPublisher.publishEvent(new UnhandledExceptionEvent(ex));
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    @Override
    protected @NotNull ResponseEntity<Object> createResponseEntity(
        @Nullable Object body,
        @NotNull HttpHeaders headers,
        @NotNull HttpStatusCode statusCode,
        @NotNull WebRequest request
    ) {
        setOriginalInstance(request, (ProblemDetail) castNonNull(body)); // body is never null here
        return new ResponseEntity<>(body, headers, statusCode);
    }

    private static void setOriginalInstance(@NotNull WebRequest request, ProblemDetail problemDetail) {
        Map<String, Object> errorAttributes = ERROR_ATTRIBUTES_PROVIDER.getErrorAttributes(
            request,
            ErrorAttributeOptions.defaults()
        );
        Object path = errorAttributes.get("path");
        if (path != null) {
            problemDetail.setInstance(URI.create(path.toString()));
        }
    }
}
