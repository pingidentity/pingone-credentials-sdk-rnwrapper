package com.pingidentity.credentials.wallet.cache;

import java.util.LinkedList;

public class SimpleQueue<T> {
    private final LinkedList<T> queue = new LinkedList<>();

    public synchronized void offer(T item) {
        queue.addLast(item);
    }

    public synchronized T poll() {
        return queue.isEmpty() ? null : queue.removeFirst();
    }

    public synchronized int size() {
        return queue.size();
    }
}
