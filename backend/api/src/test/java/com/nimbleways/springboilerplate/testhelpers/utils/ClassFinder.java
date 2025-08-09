package com.nimbleways.springboilerplate.testhelpers.utils;

import static com.nimbleways.springboilerplate.Application.BASE_PACKAGE_NAME;

import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.stream.Stream;
import org.eclipse.collections.api.list.ImmutableList;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.SimpleBeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AssignableTypeFilter;

public final class ClassFinder {

    private static final int BASE_PACKAGE_DEPTH = BASE_PACKAGE_NAME.split("\\.").length;

    private ClassFinder() {}

    public static ImmutableList<Class<?>> findAllNonAbstractExceptions(String packageName) {
        return Immutable.list.fromStream(
            getTypesAssignableTo(Throwable.class, packageName).filter(
                c -> !isTestClass(c) && !Modifier.isPrivate(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers())
            )
        );
    }

    public static <T> Stream<Class<? extends T>> getTypesAssignableTo(Class<T> targetType, String packageName) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(
            new SimpleBeanDefinitionRegistry(),
            false
        );
        scanner.addIncludeFilter(new AssignableTypeFilter(targetType));
        return scanner
            .findCandidateComponents(packageName)
            .stream()
            .map(ClassFinder::getClassOrNull)
            .filter(Objects::nonNull)
            .map(clazz -> {
                // This cast is safe because we filtered with AssignableTypeFilter
                @SuppressWarnings("unchecked")
                Class<? extends T> typedClass = (Class<? extends T>) clazz;
                return typedClass;
            });
    }

    public static String getParentPackage(Class<?> clazz) {
        String currentPackageName = clazz.getPackageName();
        if (!currentPackageName.startsWith(BASE_PACKAGE_NAME)) {
            throw new IllegalArgumentException(
                "Current package '%s' is not under the base package '%s'".formatted(
                        currentPackageName,
                        BASE_PACKAGE_NAME
                    )
            );
        }
        String[] currentPackageNameArray = currentPackageName.split("\\.");
        String packageUnderBasePackage = currentPackageNameArray[BASE_PACKAGE_DEPTH];
        return switch (packageUnderBasePackage) {
            case "common" -> BASE_PACKAGE_NAME + ".common";
            case "features" -> BASE_PACKAGE_NAME + ".features." + currentPackageNameArray[BASE_PACKAGE_DEPTH + 1];
            default -> throw new IllegalArgumentException(
                "the package '%s' just under the base package '%s' is unknown".formatted(
                        packageUnderBasePackage,
                        BASE_PACKAGE_NAME
                    )
            );
        };
    }

    private static boolean isTestClass(Class<?> clazz) {
        String path = clazz.getResource(clazz.getSimpleName() + ".class").getPath();
        return path.contains("/test-classes/");
    }

    @Nullable private static Class<?> getClassOrNull(BeanDefinition beanDefinition) {
        try {
            return Class.forName(beanDefinition.getBeanClassName());
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
