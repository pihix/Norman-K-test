package com.nimbleways.springboilerplate.common.api.annotations;

import static org.junit.jupiter.api.Assertions.*;

import com.nimbleways.springboilerplate.common.utils.ParsableChecker;
import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import jakarta.validation.ConstraintDefinitionException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@UnitTest
class ParsableUnitTests {

    private static final String VALID_VALUE = "15";
    private static final String INVALID_VALUE = "fifteen";

    private Validator validator;

    @BeforeEach
    public void setUp() {
        try (LocalValidatorFactoryBean factoryBean = new LocalValidatorFactoryBean()) {
            factoryBean.afterPropertiesSet();
            validator = factoryBean.getValidator();
        }
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_no_violation_on_valid_objects(Function<String, Object> factory) {
        Object validObject = factory.apply(VALID_VALUE);

        Set<ConstraintViolation<Object>> violations = validator.validate(validObject);

        assertTrue(violations.isEmpty(), "There should be no validation errors for valid input.");
    }

    @ParameterizedTest
    @MethodSource("provideObjectFactories")
    void validator_returns_violations_on_types_with_invalid_field_value(Function<String, Object> factory) {
        Object invalidObject = factory.apply(INVALID_VALUE);

        Set<ConstraintViolation<Object>> violations = validator.validate(invalidObject);

        assertFalse(violations.isEmpty(), "There should be validation errors for invalid input.");
        ConstraintViolation<Object> violation = violations.iterator().next();

        assertEquals(Parsable.class, violation.getConstraintDescriptor().getAnnotation().annotationType());
        assertEquals("Invalid value", violation.getMessage());
        assertEquals("field1", violation.getPropertyPath().toString());
        assertEquals(INVALID_VALUE, violation.getInvalidValue());
    }

    @Test
    void validator_throws_if_checker_does_not_have_default_constructor() {
        InputRecordCheckerWithoutDefaultConstructor request = new InputRecordCheckerWithoutDefaultConstructor(
            VALID_VALUE
        );

        Exception exception = assertThrows(Exception.class, () -> validator.validate(request));

        assertEquals(ValidationException.class, exception.getClass());
        assertEquals(ConstraintDefinitionException.class, exception.getCause().getClass());
    }

    @Test
    void validator_returns_no_violation_when_value_is_null() {
        InputRecord input = new InputRecord(null);

        // Act
        Set<ConstraintViolation<InputRecord>> violations = validator.validate(input);

        assertTrue(violations.isEmpty(), "A null value should be valid by default.");
    }

    private static Stream<Arguments> provideObjectFactories() {
        return Stream.of(
            Arguments.of((Function<String, Object>) (InputClass::new)),
            Arguments.of((Function<String, Object>) (InputRecord::new)),
            Arguments.of((Function<String, Object>) (InputRecordCheckerWithValidInstanceField::new)),
            Arguments.of((Function<String, Object>) (InputRecordCheckerWithInstanceFieldOfBadType::new)),
            Arguments.of((Function<String, Object>) (InputRecordCheckerWithPrivateInstanceField::new)),
            Arguments.of((Function<String, Object>) (InputRecordCheckerWithNonStaticInstanceField::new)),
            Arguments.of((Function<String, Object>) (InputRecordCheckerWithNonFinalInstanceField::new))
        );
    }

    @Getter
    @RequiredArgsConstructor
    private static final class InputClass {

        @Parsable(Checker.class)
        private final String field1;
    }

    private record InputRecord(@Parsable(Checker.class) String field1) {}

    static final class Checker implements ParsableChecker {

        @Override
        public boolean canParse(Object value) {
            return VALID_VALUE.equals(value);
        }
    }

    private record InputRecordCheckerWithoutDefaultConstructor(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            public Checker(String param) {
                param.notify();
            }

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithValidInstanceField(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            public static final Checker INSTANCE = new Checker();

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithInstanceFieldOfBadType(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            public static final String INSTANCE = "abc";

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithPrivateInstanceField(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            @SuppressWarnings("all")
            private static final Checker INSTANCE = new Checker();

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithNonStaticInstanceField(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            @SuppressWarnings("PMD.FinalFieldCouldBeStatic")
            public final Checker INSTANCE = null;

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }

    private record InputRecordCheckerWithNonFinalInstanceField(@Parsable(Checker.class) String field1) {
        static final class Checker implements ParsableChecker {

            @SuppressWarnings("PMD.MutableStaticState")
            public static Checker INSTANCE = new Checker();

            @Override
            public boolean canParse(Object value) {
                return VALID_VALUE.equals(value);
            }
        }
    }
}
