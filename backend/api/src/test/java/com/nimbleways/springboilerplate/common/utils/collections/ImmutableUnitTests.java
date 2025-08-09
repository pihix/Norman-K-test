package com.nimbleways.springboilerplate.common.utils.collections;

import static org.junit.jupiter.api.Assertions.*;

import com.nimbleways.springboilerplate.testhelpers.annotations.UnitTest;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.set.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.Nullable;

@UnitTest
class ImmutableUnitTests {

    @Test
    void collectList_on_Iterable_returns_ImmutableList_of_mapped_elements() {
        // Arrange
        List<String> input = List.of("a", "b", "c");
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableList<Integer> result = Immutable.collectList(input, mapper);

        // Assert
        assertEquals(List.of(1, 1, 1), result);
    }

    @Test
    void collectList_on_Array_returns_ImmutableList_of_mapped_elements() {
        // Arrange
        String[] input = { "x", "yy", "z", "x" };
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableList<Integer> result = Immutable.collectList(input, mapper);

        // Assert
        assertEquals(List.of(1, 2, 1, 1), result);
    }

    @Test
    void collectSet_on_Iterable_returns_ImmutableSet_of_mapped_unique_elements() {
        // Arrange
        List<String> input = List.of("a", "bb", "c", "a");
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableSet<Integer> result = Immutable.collectSet(input, mapper);

        // Assert
        assertEquals(Set.of(1, 2), result);
    }

    @Test
    void collectSet_on_array_returns_ImmutableSet_of_mapped_unique_elements() {
        // Arrange
        String[] input = { "x", "yy", "z", "x" };
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableSet<Integer> result = Immutable.collectSet(input, mapper);

        // Assert
        assertEquals(Set.of(1, 2), result);
    }

    @Test
    void collectList_on_empty_Iterable_returns_empty_ImmutableList() {
        // Arrange
        List<String> input = List.of();
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableList<Integer> result = Immutable.collectList(input, mapper);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void collectSet_on_empty_Iterable_returns_empty_ImmutableSet() {
        // Arrange
        List<String> input = List.of();
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableSet<Integer> result = Immutable.collectSet(input, mapper);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void collectSet_on_empty_Array_returns_empty_ImmutableSet() {
        // Arrange
        String[] input = {};
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableSet<Integer> result = Immutable.collectSet(input, mapper);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void collectList_on_non_collection_Iterable_returns_ImmutableList_of_mapped_elements() {
        // Arrange
        Iterable<String> input = () -> List.of("alpha", "beta", "gamma").iterator();
        Function<String, Integer> mapper = String::length;

        // Act
        ImmutableList<Integer> result = Immutable.collectList(input, mapper);

        // Assert
        assertEquals(List.of(5, 4, 5), result);
    }

    @Test
    void collectList_on_Iterable_allocate_twice_the_memory_allocated_by_similar_array_instantiation() {
        List<@Nullable String> collection = Collections.nCopies(10_000, null);
        long baseline = getAllocatedBytesAfterWarmup(() -> new Object[collection.size()]);

        long allocatedMemory = getAllocatedBytesAfterWarmup(() -> Immutable.collectList(collection, s -> s));

        long expectedAllocatedMemory = baseline * 2;
        assertEquals(expectedAllocatedMemory, allocatedMemory, expectedAllocatedMemory * 0.05); // 5% error margin
    }

    @Test
    void collectList_on_Array_allocate_twice_the_memory_allocated_by_similar_array_instantiation() {
        Object[] collection = IntStream.rangeClosed(1, 10_000).boxed().toArray();
        long baseline = getAllocatedBytesAfterWarmup(() -> new Object[collection.length]);

        long allocatedMemory = getAllocatedBytesAfterWarmup(() -> Immutable.collectList(collection, s -> s));

        long expectedAllocatedMemory = baseline * 2;
        assertEquals(expectedAllocatedMemory, allocatedMemory, expectedAllocatedMemory * 0.05); // 5% error margin
    }

    @Test
    void collectSet_on_Iterable_allocate_less_than_four_times_the_memory_allocated_by_similar_array_instantiation() {
        float loadFactor = 0.75f; // https://stackoverflow.com/a/10901821
        int powerOf2 = 128 * 128;
        int size = (int) (powerOf2 * loadFactor);

        List<Integer> collection = IntStream.rangeClosed(1, size).boxed().collect(Collectors.toList());
        long baseline = getAllocatedBytesAfterWarmup(() -> new Object[collection.size()]);

        long allocatedMemory = getAllocatedBytesAfterWarmup(() -> Immutable.collectSet(collection, s -> s));

        double expectedAllocatedMemory = (baseline / loadFactor) + baseline + (baseline / loadFactor);
        assertEquals(expectedAllocatedMemory, allocatedMemory, expectedAllocatedMemory * 0.05); // 5% error margin
    }

    @Test
    void collectSet_on_Array_allocate_less_than_four_times_the_memory_allocated_by_similar_array_instantiation() {
        float loadFactor = 0.75f; // https://stackoverflow.com/a/10901821
        int powerOf2 = 128 * 128;
        int size = (int) (powerOf2 * loadFactor);

        Object[] collection = IntStream.rangeClosed(1, size).boxed().toArray();
        long baseline = getAllocatedBytesAfterWarmup(() -> new Object[collection.length]);

        long allocatedMemory = getAllocatedBytesAfterWarmup(() -> Immutable.collectSet(collection, s -> s));

        double expectedAllocatedMemory = (baseline / loadFactor) + baseline + (baseline / loadFactor);
        assertEquals(expectedAllocatedMemory, allocatedMemory, expectedAllocatedMemory * 0.05); // 5% error margin
    }

    private static long getAllocatedBytesAfterWarmup(Supplier<Object> method) {
        // Warming up
        method.get();

        com.sun.management.ThreadMXBean mxBean = (com.sun.management.ThreadMXBean) ManagementFactory.getThreadMXBean();
        long currentThreadId = Thread.currentThread().threadId();

        long before = mxBean.getThreadAllocatedBytes(currentThreadId);
        method.get();
        return mxBean.getThreadAllocatedBytes(currentThreadId) - before;
    }
}
