package com.nimbleways.springboilerplate.testhelpers.utils;

import com.nimbleways.springboilerplate.common.utils.collections.Immutable;
import java.util.Arrays;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;

public class Instance {

    private static final String SCOPE_NAME = "perCall";
    private static final ClearableSimpleThreadScope SCOPE = new ClearableSimpleThreadScope();
    private static final MutableMap<ImmutableSet<Class<?>>, Data> CONTEXT_MAP = new ConcurrentHashMap<>();

    public static <T> T create(Class<T> clazz) {
        Data data = CONTEXT_MAP.computeIfAbsent(Immutable.set.of(clazz), Instance::createContext);
        return getNewBean(data.context(), clazz);
    }

    public static BeanBag createBeanBag(Class<?>... classes) {
        Data data = CONTEXT_MAP.computeIfAbsent(Immutable.set.of(classes), Instance::createContext);
        return getNewBeans(data);
    }

    private static BeanBag getNewBeans(Data data) {
        ImmutableSet<Object> beans = Immutable.collectSet(data.newBeans(), data.context()::getBean);
        SCOPE.clear();
        return new BeanBag(beans);
    }

    private static <T> T getNewBean(AnnotationConfigApplicationContext ctx, Class<T> clazz) {
        T bean = ctx.getBean(clazz);
        SCOPE.clear();
        return bean;
    }

    private static Data createContext(final ImmutableSet<Class<?>> classes) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.getBeanFactory().registerScope(SCOPE_NAME, SCOPE);
        ImmutableSet<String> initialBeanNames = Immutable.collectSet(ctx.getBeanDefinitionNames(), n -> n);
        registerClassesTransitively(ctx, classes);
        ctx.refresh();
        ImmutableSet<String> newBeans = Immutable.collectSet(ctx.getBeanDefinitionNames(), n -> n).difference(
            initialBeanNames
        );
        return new Data(ctx, newBeans);
    }

    private static void registerClassesTransitively(
        AnnotationConfigApplicationContext ctx,
        Iterable<Class<?>> classes
    ) {
        for (Class<?> clazz : classes) {
            ctx.registerBean(clazz.getName(), clazz, bd -> bd.setScope(SCOPE_NAME));
            Import importAnnotation = clazz.getAnnotation(Import.class);
            if (importAnnotation != null) {
                registerClassesTransitively(ctx, Arrays.stream(importAnnotation.value())::iterator);
            }
        }
    }

    private record Data(AnnotationConfigApplicationContext context, ImmutableSet<String> newBeans) {}
}
