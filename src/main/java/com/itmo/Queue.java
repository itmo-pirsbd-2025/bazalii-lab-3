package com.itmo;

public interface Queue<E> {
    /**
     * Adds the specified element to the queue.
     */
    void enqueue(E element);

    /**
     * Retrieves the first element from the queue and returns it;
     * returns null if the queue is empty.
     */
    E dequeue();

    /**
     * Validates the data structure state at the end of execution.
     * For tests.
     */
    default void validate() {
    }
}