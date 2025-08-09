package com.nimbleways.springboilerplate.common.api.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@UnitTest
class NotEmptyEclipseCollectionUnitTests {

    private static final ImmutableList<String> NON_EMPTY_LIST = Immutable.list.of("A");
    private static final ImmutableList<String> EMPTY_LIST = Immutable.list.empty();

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean()) {
            factoryBean.afterPropertiesSet();
            validator = factoryBean.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_no_violation_on_non_empty_collections(Function<ImmutableList<String>, ?> factory) {
        Object target = factory.apply(NON_EMPTY_LIST);

        // ACT
        Set<ConstraintViolation<Object>> violations = validator.validate(target);

        assertTrue(violations.isEmpty(), "non-empty collections must pass validation");
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_violation_on_empty_collections(Function<ImmutableList<String>, ?> factory) {
        Object target = factory.apply(EMPTY_LIST);

        // ACT
        Set<ConstraintViolation<Object>> violations = validator.validate(target);

        assertFalse(violations.isEmpty(), "empty collections must fail validation");

        ConstraintViolation<Object> violation = violations.iterator().next();
        assertEquals(NotEmpty.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("must not be empty", violation.getMessage());
        assertEquals("field1", violation.getPropertyPath().toString());
        assertEquals(EMPTY_LIST, violation.getInvalidValue());
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_violation_on_null_collections(Function<ImmutableList<String>, ?> factory) {
        Object target = factory.apply(null);

        // ACT
        Set<ConstraintViolation<Object>> violations = validator.validate(target);

        assertFalse(violations.isEmpty(), "null collections must fail validation");

        ConstraintViolation<Object> violation = violations.iterator().next();
        assertEquals(
            NotEmpty.class,
            violation.getConstraintDescriptor().getAnnotation().annotationType(),
            "The violation should be from @NotEmpty"
        );
        assertEquals("must not be empty", violation.getMessage(), "The error message should be correct");
        assertEquals("field1", violation.getPropertyPath().toString(), "The property path should be 'field1'");
        assertNull(violation.getInvalidValue(), "The invalid value that was passed should be null");
    }

    private static Stream<Arguments> provideObjectFactories() {
        return Stream.of(
            Arguments.of((Function<ImmutableList<String>, ?>) InputClass::new),
            Arguments.of((Function<ImmutableList<String>, ?>) InputRecord::new)
        );
    }

    @Getter
    @RequiredArgsConstructor
    private static final class InputClass {

        @NotEmpty private final ImmutableList<String> field1;
    }

    private record InputRecord(@NotEmpty ImmutableList<String> field1) {}
}
