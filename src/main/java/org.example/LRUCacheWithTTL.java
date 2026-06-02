package org.example;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

public class LRUCacheWithTTL {

    private static class Node {
        int key;
        int value;
        Instant expiry;
        Node prev;
        Node next;
    }

    private final Node head = new Node();
    private final Node tail = new Node();

    private final int capacity;
    private final Duration ttl;
    private final Clock clock;
    private final Map<Integer, Node> cache = new HashMap<>();
    private final PriorityQueue<Node> expiryQueue = new PriorityQueue<>(
            (a, b) -> a.expiry.compareTo(b.expiry)
    );

    public LRUCacheWithTTL(int capacity, int ttlSeconds) {
        this(capacity, ttlSeconds, Clock.systemUTC());
    }

    public LRUCacheWithTTL(int capacity, int ttlSeconds, Clock clock) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }

        this.capacity = capacity;
        this.ttl = Duration.ofSeconds(ttlSeconds);
        this.clock = clock;
        head.next = tail;
        tail.prev = head;
    }

    public Optional<Integer> get(int key) {
        purgeExpired();
        if (!cache.containsKey(key)) {
            return Optional.empty();
        }
        Node node = cache.get(key);
        remove(node);
        insert(node);
        return Optional.of(node.value);
    }

    public void put(int key, int value) {
        purgeExpired();
        if (cache.containsKey(key)) {
            Node old = cache.get(key);
            remove(old);
            cache.remove(key);
            // old's heap entry is left in place and skipped lazily in purgeExpired
        }

        Node node = new Node();
        node.key = key;
        node.value = value;
        node.expiry = Instant.now(clock).plus(ttl);
        cache.put(key, node);
        insert(node);
        expiryQueue.offer(node);

        if (cache.size() > capacity) {
            Node last = tail.prev;
            remove(last);
            cache.remove(last.key);
            // last's heap entry is left in place and skipped lazily in purgeExpired
        }
    }

    private void purgeExpired() {
        Instant now = Instant.now(clock);
        while (!expiryQueue.isEmpty() && !expiryQueue.peek().expiry.isAfter(now)) {
            Node node = expiryQueue.poll();
            // skip stale entries: nodes evicted by LRU or superseded by a put update
            if (cache.get(node.key) == node) {
                remove(node);
                cache.remove(node.key);
            }
        }
    }

    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private void insert(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }
}
