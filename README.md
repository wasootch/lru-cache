# Cache Implementations

Java implementations of LRU, LFU, and LRU-with-TTL caches, all with O(1) `get` and `put` operations.

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

## LRU Cache with TTL

Extends LRU eviction with a per-entry time-to-live. Expired entries are treated as absent on `get` and are purged proactively before every `get` and `put`.

Adds a min-heap (`PriorityQueue`) ordered by expiry time alongside the standard LRU structures. Before each operation, expired entries are drained from the heap front and removed from both the map and linked list. This prevents expired entries from occupying capacity slots and forcing valid entries to be evicted prematurely.

Heap entries that become stale (due to LRU eviction or a `put` update) are handled with lazy deletion: when an entry is popped from the heap, an identity check against the map confirms it is still the active node before removal.

Accepts an optional `java.time.Clock` for deterministic testing without `Thread.sleep`.

- `get(key)` — purges expired entries, then returns `Optional.of(value)` if present and fresh, `Optional.empty()` otherwise.
- `put(key, value)` — purges expired entries, then inserts or replaces the entry with a fresh TTL. Evicts the least-recently-used entry if still over capacity after purging.

## Running Tests

```
mvn test
```
