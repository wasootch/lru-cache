package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

///
/// LFUCache is a series of LRUCache with each representing a frequency of use.
///
public class LFUCache {

    private static class Node {
        int key;
        int value;
        int freq;
        Node prev;
        Node next;
    }

    private static class LRUCache {
        private final Node head = new Node();
        private final Node tail = new Node();
        private int size;

        LRUCache() {
            head.next = tail;
            tail.prev = head;
        }

        void insertFront(Node node) {
            node.next = head.next;
            node.prev = head;
            head.next.prev = node;
            head.next = node;
            size++;
        }

        void remove(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
            size--;
        }

        Node removeLast() {
            Node last = tail.prev;
            remove(last);
            return last;
        }

        boolean isEmpty() {
            return size == 0;
        }
    }

    private final int capacity;
    private int minFrequency;
    private final Map<Integer, Node> keyMap = new HashMap<>();
    private final Map<Integer, LRUCache> freqencyMap = new HashMap<>();

    public LFUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive");
        }
        this.capacity = capacity;
    }

    public Optional<Integer> get(int key) {
        if (!keyMap.containsKey(key)) {
            return Optional.empty();
        }
        Node node = keyMap.get(key);
        incrementFreq(node);
        return Optional.of(node.value);
    }

    public void put(int key, int value) {
        if (keyMap.containsKey(key)) {
            Node node = keyMap.get(key);
            node.value = value;
            incrementFreq(node);
            return;
        }

        if (keyMap.size() == capacity) {
            Node evicted = freqencyMap.get(minFrequency).removeLast();
            keyMap.remove(evicted.key);
        }

        Node node = new Node();
        node.key = key;
        node.value = value;
        node.freq = 1;
        keyMap.put(key, node);
        freqencyMap.computeIfAbsent(1, k -> new LRUCache()).insertFront(node);
        minFrequency = 1;
    }

    private void incrementFreq(Node node) {
        LRUCache bucket = freqencyMap.get(node.freq);
        bucket.remove(node);
        if (bucket.isEmpty() && node.freq == minFrequency) {
            minFrequency++;
        }
        node.freq++;
        freqencyMap.computeIfAbsent(node.freq, k -> new LRUCache()).insertFront(node);
    }
}
