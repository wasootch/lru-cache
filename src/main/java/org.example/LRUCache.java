package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LRUCache {

    private static class Node {
        int key;
        int value;
        Node prev;
        Node next;
    }

    private final Node head = new Node();
    private final Node tail = new Node();

    private final int capacity;
    private final Map<Integer, Node> cache = new HashMap<>();

    public LRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + capacity);
        }

        this.capacity = capacity;

        head.next = tail;
        tail.prev = head;
    }

    public Optional<Integer> get(int key) {
        if (!cache.containsKey(key)) {
            return Optional.empty();
        }

        Node node = cache.get(key);
        remove(node);
        insert(node);
        return Optional.of(node.value);
    }

    public void put(int key, int value) {
        if (cache.containsKey(key)) {
            Node node = cache.get(key);
            node.value = value;
            remove(node);
            insert(node);
            return;
        }

        Node node = new Node();
        node.key = key;
        node.value = value;
        cache.put(key, node);
        insert(node);

        if (cache.size() > capacity) {
            Node last = tail.prev;
            remove(last);
            cache.remove(last.key);
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
