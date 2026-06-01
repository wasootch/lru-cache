# LRU Cache

A Java implementation of a Least Recently Used (LRU) cache with O(1) `get` and `put` operations.

## Implementation

Combines a `HashMap` for O(1) key lookup with a doubly-linked list to track access order. Sentinel head and tail nodes simplify edge cases by eliminating null checks on insert and remove.

- `get(key)` — returns `Optional.of(value)` if present, `Optional.empty()` if not. Promotes the accessed entry to most-recently-used.
- `put(key, value)` — inserts or updates an entry and promotes it to most-recently-used. Evicts the least-recently-used entry when over capacity.

Both operations run in **O(1)** time and space.

## Running Tests

```
mvn test
```
