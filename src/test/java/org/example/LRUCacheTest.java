package org.example;

import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;

public class LRUCacheTest {

    @Test
    public void testLRUCache_leetcodeExample() {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        assertEquals(cache.get(1), Optional.of(1));
        cache.put(3, 3);
        assertEquals(cache.get(2), Optional.empty());
        cache.put(4, 4);
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(3), Optional.of(3));
        assertEquals(cache.get(4), Optional.of(4));
    }

    @Test
    public void testGet_emptyCache_returnsEmpty() {
        LRUCache cache = new LRUCache(2);
        assertEquals(cache.get(1), Optional.empty());
    }

    @Test
    public void testGet_missingKey_returnsEmpty() {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 10);
        assertEquals(cache.get(99), Optional.empty());
    }

    @Test
    public void testPutAndGet_basic() {
        LRUCache cache = new LRUCache(2);
        cache.put(5, 42);
        assertEquals(cache.get(5), Optional.of(42));
    }

    @Test
    public void testPut_updateExistingKey_updatesValue() {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1);
        cache.put(1, 99);
        assertEquals(cache.get(1), Optional.of(99));
    }

    @Test
    public void testPut_updateExistingKey_doesNotEvict() {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(1, 100);
        assertEquals(cache.get(2), Optional.of(2));
        assertEquals(cache.get(1), Optional.of(100));
    }

    @Test
    public void testCapacityOne_evictsOnSecondPut() {
        LRUCache cache = new LRUCache(1);
        cache.put(1, 10);
        cache.put(2, 20);
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(2), Optional.of(20));
    }

    @Test
    public void testCapacityOne_updateOnlyKey() {
        LRUCache cache = new LRUCache(1);
        cache.put(1, 10);
        cache.put(1, 99);
        assertEquals(cache.get(1), Optional.of(99));
    }

    @Test
    public void testGet_refreshesRecency() {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.get(1);
        cache.put(3, 3);
        assertEquals(cache.get(1), Optional.of(1));
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(3), Optional.of(3));
    }

    @Test
    public void testPut_updateRefreshesRecency() {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(1, 100);
        cache.put(3, 3);
        assertEquals(cache.get(1), Optional.of(100));
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(3), Optional.of(3));
    }

    @Test
    public void testEviction_lruOrder() {
        LRUCache cache = new LRUCache(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.get(1);
        cache.get(2);
        cache.put(4, 4);
        assertEquals(cache.get(3), Optional.empty());
        assertEquals(cache.get(1), Optional.of(1));
        assertEquals(cache.get(2), Optional.of(2));
        assertEquals(cache.get(4), Optional.of(4));
    }

    @Test
    public void testPut_negativeValue_distinguishedFromNotFound() {
        LRUCache cache = new LRUCache(2);
        cache.put(1, -1);
        assertEquals(cache.get(1), Optional.of(-1));  // found, value happens to be -1
        assertEquals(cache.get(2), Optional.empty());  // not found
    }

    @Test
    public void testPut_keyZero() {
        LRUCache cache = new LRUCache(2);
        cache.put(0, 5);
        assertEquals(cache.get(0), Optional.of(5));
    }

    @Test
    public void testPut_reinsertEvictedKey() {
        LRUCache cache = new LRUCache(1);
        cache.put(1, 10);
        cache.put(2, 20);
        cache.put(1, 99);
        assertEquals(cache.get(1), Optional.of(99));
        assertEquals(cache.get(2), Optional.empty());
    }

    @Test
    public void testEviction_sequentialEvictions() {
        LRUCache cache = new LRUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.put(4, 4);
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(3), Optional.of(3));
        assertEquals(cache.get(4), Optional.of(4));
    }
}