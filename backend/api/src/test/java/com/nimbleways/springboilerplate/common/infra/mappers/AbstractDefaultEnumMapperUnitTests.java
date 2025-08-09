package com.nimbleways.springboilerplate.common.infra.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import org.junit.jupiter.api.Test;

@UnitTest
class AbstractDefaultEnumMapperUnitTests {

    @Test
    void cannot_parse_non_string_objects() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();
        boolean canParse = testEnumMapper.canParse(43);
        assertFalse(canParse);
    }

    @Test
    void can_parse_string_with_valid_value() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();
        boolean canParse = testEnumMapper.canParse("VALID_VALUE");
        assertTrue(canParse);
    }

    @Test
    void can_parse_string_with_invalid_value() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();
        boolean canParse = testEnumMapper.canParse("INVALID_VALUE");
        assertFalse(canParse);
    }

    @Test
    void toValueObject_with_valid_value_returns_enum_object() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();
        TestEnum value = testEnumMapper.toValueObject("VALID_VALUE");
        assertEquals(TestEnum.VALID_VALUE, value);
    }

    @Test
    void toValueObject_with_invalid_value_throws_IllegalArgumentException() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();

        Exception exception = assertThrows(Exception.class, () -> testEnumMapper.toValueObject("INVALID_VALUE"));

        assertEquals(IllegalArgumentException.class, exception.getClass());
    }

    @Test
    void fromValueObject_returns_enum_name() {
        TestEnumMapper testEnumMapper = new TestEnumMapper();

        String stringValue = testEnumMapper.fromValueObject(TestEnum.VALID_VALUE);

        assertEquals("VALID_VALUE", stringValue);
    }

    private enum TestEnum {
        VALID_VALUE,
    }

    private static class TestEnumMapper extends AbstractDefaultEnumMapper<TestEnum> {

        protected TestEnumMapper() {
            super(TestEnum.class);
        }
    }
}
