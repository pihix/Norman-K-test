package com.nimbleways.springboilerplate.transverse.architecture;

import static com.nimbleways.springboilerplate.Application.BASE_PACKAGE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import com.nimbleways.springboilerplate.testhelpers.baseclasses.exceptionhandling.BaseExceptionHandlerIntegrationTests;
import com.nimbleways.springboilerplate.testhelpers.junitextensions.FailFastExtension;
import com.nimbleways.springboilerplate.testhelpers.utils.ClassFinder;
import java.lang.reflect.*;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(FailFastExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ExceptionHandlerRulesUnitTests {

    @ParameterizedTest
    @MethodSource("getParentPackagesContainingExceptions")
    @Order(1)
    void all_parent_packages_defining_custom_exceptions_must_have_an_exception_handling_test_classes(
        String parentPackage
    ) {
        ImmutableList<Class<? extends BaseExceptionHandlerIntegrationTests>> exceptionHandlerTestClasses =
            getExceptionHandlerTestClasses(parentPackage);
        assertNotEquals(
            0,
            exceptionHandlerTestClasses.size(),
            "ExceptionHandler test class is missing in package " + parentPackage
        );
        assertEquals(
            1,
            exceptionHandlerTestClasses.size(),
            "There should be only one ExceptionHandler test class in package " + parentPackage
        );
    }

    @ParameterizedTest
    @MethodSource("getParentPackagesContainingExceptions")
    @Order(2)
    void exception_handler_test_classes_have_a_staticProvideExceptions_method(String parentPackage) {
        ImmutableList<Class<? extends BaseExceptionHandlerIntegrationTests>> exceptionHandlerTestClasses =
            getExceptionHandlerTestClasses(parentPackage);
        assertTrue(
            hasStaticProvideExceptionsMethod(exceptionHandlerTestClasses.getOnly()),
            "ExceptionHandler test class must have a method matching this signature: static Stream<Arguments> staticProvideExceptions()"
        );
    }

    @ParameterizedTest
    @MethodSource("getParentPackagesContainingExceptions")
    @Order(3)
    void exception_handler_test_classes_have_a_getExceptions_method_that_returns_staticProvideExceptions_result(
        String parentPackage
    ) {
        ImmutableList<Class<? extends BaseExceptionHandlerIntegrationTests>> exceptionHandlerTestClasses =
            getExceptionHandlerTestClasses(parentPackage);
        assertGetExceptionsMethod(exceptionHandlerTestClasses.getOnly());
    }

    private static ImmutableList<Class<? extends BaseExceptionHandlerIntegrationTests>> getExceptionHandlerTestClasses(
        String parentPackage
    ) {
        return Immutable.list.fromStream(
            ClassFinder.getTypesAssignableTo(BaseExceptionHandlerIntegrationTests.class, parentPackage)
        );
    }

    private static Stream<String> getParentPackagesContainingExceptions() {
        return ClassFinder.findAllNonAbstractExceptions(BASE_PACKAGE_NAME)
            .stream()
            .map(ClassFinder::getParentPackage)
            .distinct();
    }

    public boolean hasStaticProvideExceptionsMethod(Class<?> clazz) {
        try {
            Method method = clazz.getDeclaredMethod("staticProvideExceptions");
            if (
                !method.getReturnType().equals(Stream.class) ||
                !Modifier.isStatic(method.getModifiers()) ||
                method.getParameterCount() != 0
            ) {
                return false;
            }

            // Check if the generic return type is Stream<Arguments>
            Type returnType = method.getGenericReturnType();
            if (returnType instanceof ParameterizedType paramType) {
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length == 1) {
                    return typeArgs[0].equals(Arguments.class);
                }
            }

            return false;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public void assertGetExceptionsMethod(Class<?> clazz) {
        try {
            Method getExceptionsMethod = clazz.getDeclaredMethod("getExceptions");
            getExceptionsMethod.setAccessible(true);
            Method staticMethod = clazz.getDeclaredMethod("staticProvideExceptions");
            staticMethod.setAccessible(true);

            Object instance = create(clazz);

            Stream<Arguments> resultFromGetExceptions = (Stream<Arguments>) getExceptionsMethod.invoke(instance);
            Stream<Arguments> resultFromStaticMethod = (Stream<Arguments>) staticMethod.invoke(clazz);

            List<Arguments> list1 = resultFromGetExceptions.toList();
            List<Arguments> list2 = resultFromStaticMethod.toList();

            assertThat(list1)
                .withFailMessage("getExceptions() must return the result of staticProvideExceptions()")
                .usingRecursiveComparison()
                .isEqualTo(list2);
        } catch (Exception e) {
            log.error("assertGetExceptionsMethod failed", e);
            fail(e.getMessage());
        }
    }

    private <T> T create(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true); // explicitly allow access
            return constructor.newInstance();
        } catch (
            InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e
        ) {
            throw new IllegalArgumentException(e);
        }
    }
}
