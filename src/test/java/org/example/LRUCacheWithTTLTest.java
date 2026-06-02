package org.example;

import org.testng.annotations.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.testng.Assert.assertEquals;

public class LRUCacheWithTTLTest {

    private static class AdjustableClock extends Clock {
        private Instant now = Instant.now();

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override public ZoneOffset getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    // --- Basic LRU behavior (TTL set large enough to not affect these tests) ---

    @Test
    public void testGet_emptyCache_returnsEmpty() {
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60);
        assertEquals(cache.get(1), Optional.empty());
    }

    @Test
    public void testGet_missingKey_returnsEmpty() {
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60);
        cache.put(1, 10);
        assertEquals(cache.get(99), Optional.empty());
    }

    @Test
    public void testPutAndGet_basic() {
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60);
        cache.put(1, 42);
        assertEquals(cache.get(1), Optional.of(42));
    }

    @Test
    public void testPut_updateExistingKey_updatesValue() {
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60);
        cache.put(1, 1);
        cache.put(1, 99);
        assertEquals(cache.get(1), Optional.of(99));
    }

    @Test
    public void testPut_updateExistingKey_doesNotEvict() {
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(1, 100);
        assertEquals(cache.get(2), Optional.of(2));
        assertEquals(cache.get(1), Optional.of(100));
    }

    @Test
    public void testEviction_lruOrder() {
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.get(1);
        cache.put(3, 3);
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(1), Optional.of(1));
        assertEquals(cache.get(3), Optional.of(3));
    }

    @Test
    public void testCapacityOne_evictsOnSecondPut() {
        LRUCacheWithTTL cache = new LRUCacheWithTTL(1, 60);
        cache.put(1, 10);
        cache.put(2, 20);
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(2), Optional.of(20));
    }

    // --- TTL behavior ---

    @Test
    public void testGet_withinTTL_returnsValue() {
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60);
        cache.put(1, 10);
        assertEquals(cache.get(1), Optional.of(10));
    }

    @Test
    public void testGet_afterTTLExpiry_returnsEmpty() {
        AdjustableClock clock = new AdjustableClock();
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60, clock);
        cache.put(1, 10);
        clock.advance(Duration.ofSeconds(61));
        assertEquals(cache.get(1), Optional.empty());
    }

    @Test
    public void testPut_update_resetsTTL() {
        AdjustableClock clock = new AdjustableClock();
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60, clock);
        cache.put(1, 10);
        clock.advance(Duration.ofSeconds(45));
        cache.put(1, 99);                          // resets TTL for another 60 seconds
        clock.advance(Duration.ofSeconds(45));     // 45s past original expiry, within new window
        assertEquals(cache.get(1), Optional.of(99));
    }

    @Test
    public void testPut_onExpiredKey_refreshes() {
        AdjustableClock clock = new AdjustableClock();
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60, clock);
        cache.put(1, 10);
        clock.advance(Duration.ofSeconds(61));
        cache.put(1, 99);
        assertEquals(cache.get(1), Optional.of(99));
    }

    @Test
    public void testExpiredEntries_purgdProactivelyOnPut() {
        // With Option B, expired entries are purged from the heap during put(), so they
        // no longer occupy capacity and do not force valid entries to be evicted.
        AdjustableClock clock = new AdjustableClock();
        LRUCacheWithTTL cache = new LRUCacheWithTTL(2, 60, clock);
        cache.put(1, 10);
        cache.put(2, 20);
        clock.advance(Duration.ofSeconds(61));     // both entries expired
        cache.put(3, 30);                          // purges 1 and 2, inserts 3 — no LRU eviction needed
        cache.put(4, 40);                          // purges nothing new, inserts 4 — no LRU eviction needed
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(3), Optional.of(30));
        assertEquals(cache.get(4), Optional.of(40));
    }

    @Test
    public void testExpiredEntry_afterGet_doesNotOccupyCapacity() {
        AdjustableClock clock = new AdjustableClock();
        LRUCacheWithTTL cache = new LRUCacheWithTTL(1, 60, clock);
        cache.put(1, 10);
        clock.advance(Duration.ofSeconds(61));
        cache.get(1);                              // returns empty; expired entry cleaned up
        cache.put(2, 20);                          // should succeed without evicting itself
        assertEquals(cache.get(2), Optional.of(20));
    }

    @Test
    public void testExpiredEntry_doesNotBlockNewEntry() {
        AdjustableClock clock = new AdjustableClock();
        LRUCacheWithTTL cache = new LRUCacheWithTTL(1, 60, clock);
        cache.put(1, 10);
        clock.advance(Duration.ofSeconds(61));
        cache.put(2, 20);                          // expired key 1 purged; key 2 inserted without evicting itself
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(2), Optional.of(20));
    }
}
