package org.example;

import org.testng.annotations.Test;

import java.util.Optional;

import static org.testng.Assert.assertEquals;

public class LFUCacheTest {

    @Test
    public void testLFUCache_leetcodeExample() {
        LFUCache cache = new LFUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        assertEquals(cache.get(1), Optional.of(1));   // key 1 freq → 2
        cache.put(3, 3);                               // evicts key 2 (freq 1), not key 1 (freq 2)
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(3), Optional.of(3));
        cache.put(4, 4);                               // get(3) raised key 3 to freq=2, so both 1 and 3 are freq=2;
                                                       // key 1 is LRU among them and is evicted
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(3), Optional.of(3));
        assertEquals(cache.get(4), Optional.of(4));
    }

    @Test
    public void testGet_emptyCache_returnsEmpty() {
        LFUCache cache = new LFUCache(2);
        assertEquals(cache.get(1), Optional.empty());
    }

    @Test
    public void testGet_missingKey_returnsEmpty() {
        LFUCache cache = new LFUCache(2);
        cache.put(1, 10);
        assertEquals(cache.get(99), Optional.empty());
    }

    @Test
    public void testPutAndGet_basic() {
        LFUCache cache = new LFUCache(2);
        cache.put(5, 42);
        assertEquals(cache.get(5), Optional.of(42));
    }

    @Test
    public void testPut_updateExistingKey_updatesValue() {
        LFUCache cache = new LFUCache(2);
        cache.put(1, 1);
        cache.put(1, 99);
        assertEquals(cache.get(1), Optional.of(99));
    }

    @Test
    public void testEviction_byFrequency_notRecency() {
        // Key 1 is accessed more than key 2, so key 2 is evicted even though key 1 is older.
        LFUCache cache = new LFUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.get(1);
        cache.get(1);                                  // key 1 freq=3, key 2 freq=1
        cache.put(3, 3);                               // evicts key 2
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(1), Optional.of(1));
        assertEquals(cache.get(3), Optional.of(3));
    }

    @Test
    public void testEviction_lruTiebreaking() {
        // Keys 1 and 2 both have freq 1; key 1 is least recently used so it is evicted.
        LFUCache cache = new LFUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);                               // evicts key 1 (freq 1, LRU among ties)
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(2), Optional.of(2));
        assertEquals(cache.get(3), Optional.of(3));
    }

    @Test
    public void testPut_updateIncreasesFrequency() {
        // Updating an existing key raises its frequency, protecting it from eviction.
        LFUCache cache = new LFUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(1, 100);                             // key 1 freq → 2
        cache.put(3, 3);                               // evicts key 2 (freq 1)
        assertEquals(cache.get(1), Optional.of(100));
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(3), Optional.of(3));
    }

    @Test
    public void testCapacityOne_evictsOnSecondPut() {
        LFUCache cache = new LFUCache(1);
        cache.put(1, 10);
        cache.put(2, 20);
        assertEquals(cache.get(1), Optional.empty());
        assertEquals(cache.get(2), Optional.of(20));
    }

    @Test
    public void testCapacityOne_updateOnlyKey() {
        LFUCache cache = new LFUCache(1);
        cache.put(1, 10);
        cache.put(1, 99);
        assertEquals(cache.get(1), Optional.of(99));
    }

    @Test
    public void testGet_refreshesFrequency() {
        // Frequently accessed keys survive eviction over less-accessed ones.
        LFUCache cache = new LFUCache(3);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.put(3, 3);
        cache.get(1);
        cache.get(1);                                  // key 1 freq=3
        cache.get(2);                                  // key 2 freq=2, key 3 freq=1
        cache.put(4, 4);                               // evicts key 3
        assertEquals(cache.get(3), Optional.empty());
        assertEquals(cache.get(1), Optional.of(1));
        assertEquals(cache.get(2), Optional.of(2));
        assertEquals(cache.get(4), Optional.of(4));
    }

    @Test
    public void testMinFreq_advancesAfterBucketEmpties() {
        // After the sole entry at minFreq is evicted, minFreq must advance correctly
        // so that the next eviction targets the right bucket.
        LFUCache cache = new LFUCache(2);
        cache.put(1, 1);
        cache.put(2, 2);
        cache.get(1);                                  // key 1 freq=2, key 2 freq=1, minFreq=1
        cache.put(3, 3);                               // evicts key 2 (minFreq=1), minFreq resets to 1
        cache.put(4, 4);                               // evicts key 3 (freq=1)
        assertEquals(cache.get(2), Optional.empty());
        assertEquals(cache.get(3), Optional.empty());
        assertEquals(cache.get(1), Optional.of(1));
        assertEquals(cache.get(4), Optional.of(4));
    }

    @Test
    public void testPut_negativeValue_distinguishedFromNotFound() {
        LFUCache cache = new LFUCache(2);
        cache.put(1, -1);
        assertEquals(cache.get(1), Optional.of(-1));
        assertEquals(cache.get(2), Optional.empty());
    }

    @Test
    public void testPut_keyZero() {
        LFUCache cache = new LFUCache(2);
        cache.put(0, 5);
        assertEquals(cache.get(0), Optional.of(5));
    }
}
