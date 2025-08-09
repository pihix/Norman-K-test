package com.nimbleways.springboilerplate.testhelpers.utils;

import lombok.RequiredArgsConstructor;
import org.eclipse.collections.api.set.ImmutableSet;

@RequiredArgsConstructor
public class BeanBag {

    private final ImmutableSet<Object> beans;

    public <T> T get(Class<T> beanType) {
        return beans.selectInstancesOf(beanType).getOnly();
    }
}
