package com.itmo;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class FAABasedQueue<E> implements Queue<E> {

    private final InfiniteLinkedList infiniteArray = new InfiniteLinkedList();
    private final AtomicLong enqIdx = new AtomicLong(0);
    private final AtomicLong deqIdx = new AtomicLong(0);

    private static final int SEGMENT_SIZE = 2;

    private static final Object POISONED = new Object();

    @Override
    public void enqueue(E element) {
        for (; ; ) {
            long index = enqIdx.getAndIncrement();
            int segmentIndex = (int) (index % SEGMENT_SIZE);
            Segment segment = infiniteArray.getSegment(index);

            if (segment.cells.compareAndSet(segmentIndex, null, element)) {
                return;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public E dequeue() {
        for (; ; ) {
            if (deqIdx.get() >= enqIdx.get()) {
                return null;
            }

            long index = deqIdx.getAndIncrement();
            int segmentIndex = (int) (index % SEGMENT_SIZE);
            Segment segment = infiniteArray.getSegment(index);

            if (segment.cells.compareAndSet(segmentIndex, null, POISONED)) {
                continue;
            }

            E result = (E) segment.cells.get(segmentIndex);
            if (!segment.cells.compareAndSet(segmentIndex, result, null)) {
                continue;
            }

            return result;
        }
    }

    private class InfiniteLinkedList {
        private final AtomicReference<Segment> head;

        InfiniteLinkedList() {
            Segment firstSegment = new Segment(0);
            this.head = new AtomicReference<>(firstSegment);
        }

        Segment getSegment(long index) {
            Segment currentHead = head.get();
            long wantedSegmentIndex = index / SEGMENT_SIZE;

            while (currentHead.id < wantedSegmentIndex && currentHead.next.get() != null) {
                currentHead = currentHead.next.get();
            }

            if (currentHead.id == wantedSegmentIndex) {
                return currentHead;
            }

            Segment next = currentHead.next.get();
            if (next != null) {
                return next;
            }

            Segment nextSegment = new Segment(currentHead.id + 1);
            currentHead.next.compareAndSet(null, nextSegment);

            return currentHead.next.get();
        }
    }

    private static class Segment {
        final long id;
        final AtomicReference<Segment> next = new AtomicReference<>(null);
        final AtomicReferenceArray<Object> cells;

        Segment(long id) {
            this.id = id;
            this.cells = new AtomicReferenceArray<>(SEGMENT_SIZE);
        }
    }
}