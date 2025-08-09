package com.nimbleways.springboilerplate.testhelpers.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.core.NamedThreadLocal;
import org.springframework.lang.Nullable;

// fork of org.springframework.context.support.SimpleThreadScope
public final class ClearableProxiedThreadScope implements Scope {

    private final ThreadLocal<Map<String, BeanData>> threadScope = NamedThreadLocal.withInitial(
        "ClearableProxiedThreadScope",
        HashMap::new
    );

    @Override
    public @NotNull Object get(@NotNull String name, @NotNull ObjectFactory<?> objectFactory) {
        Map<String, BeanData> beanDataMap = this.threadScope.get();
        BeanData beanData = beanDataMap.get(name);
        if (beanData == null) {
            beanData = createBeanData(objectFactory);
            beanDataMap.put(name, beanData);
        } else if (beanData.isStale()) {
            beanData.useNewTarget(objectFactory);
        }
        return beanData.proxyBean();
    }

    @Override
    @Nullable public Object remove(@NotNull String name) {
        Map<String, BeanData> scope = this.threadScope.get();
        return scope.remove(name);
    }

    @Override
    public void registerDestructionCallback(@NotNull String name, @NotNull Runnable callback) {}

    @Override
    @Nullable public Object resolveContextualObject(@NotNull String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return Thread.currentThread().getName();
    }

    public void clear() {
        Map<String, BeanData> beanDataMap = threadScope.get();
        for (BeanData beanData : beanDataMap.values()) {
            beanData.markAsStale();
        }
    }

    private static BeanData createBeanData(ObjectFactory<?> objectFactory) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(objectFactory.getObject());
        StaleBeanCheckAdvice advice = new StaleBeanCheckAdvice();
        proxyFactory.addAdvice(advice);
        return new BeanData(proxyFactory, advice, proxyFactory.getProxy());
    }

    private record BeanData(ProxyFactory proxyFactory, StaleBeanCheckAdvice staleAdvice, Object proxyBean) {
        public void useNewTarget(ObjectFactory<?> objectFactory) {
            proxyFactory.setTarget(objectFactory.getObject());
            staleAdvice().stale(false);
        }
        public void markAsStale() {
            staleAdvice().stale(true);
        }
        public boolean isStale() {
            return staleAdvice().stale();
        }
    }

    @Getter
    @Setter
    private static final class StaleBeanCheckAdvice implements MethodBeforeAdvice {

        private boolean stale = false;

        @Override
        public void before(@NotNull Method method, @NotNull Object[] args, Object target) {
            if (stale) {
                throw new IllegalStateException("This is a stale instance.");
            }
        }
    }
}
