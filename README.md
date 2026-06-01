# Cache Implementations

Java implementations of LRU and LFU caches, both with O(1) `get` and `put` operations.

## LRU Cache

Evicts the **least recently used** entry when over capacity.

Combines a `HashMap` for O(1) key lookup with a doubly-linked list to track access order. Sentinel head and tail nodes simplify edge cases by eliminating null checks on insert and remove.

- `get(key)` — returns `Optional.of(value)` if present, `Optional.empty()` if not. Promotes the entry to most-recently-used.
- `put(key, value)` — inserts or updates an entry and promotes it to most-recently-used. Evicts the least-recently-used entry when over capacity.

## LFU Cache

Evicts the **least frequently used** entry when over capacity. Ties in frequency are broken by recency (least recently used among tied entries is evicted first).

Uses a `HashMap` for O(1) key lookup, a second `HashMap` keyed by frequency where each bucket is an LRU-ordered doubly-linked list, and a `minFreq` pointer for O(1) eviction. Each frequency bucket behaves like a small LRU cache — new and promoted entries are inserted at the head, and the eviction candidate is always the tail of the minimum-frequency bucket.

- `get(key)` — returns `Optional.of(value)` if present, `Optional.empty()` if not. Increments the entry's frequency.
- `put(key, value)` — inserts or updates an entry and increments its frequency. New entries start at frequency 1. Evicts the least-frequently-used (LRU-tiebroken) entry when over capacity.

## Running Tests

```
mvn test
```
