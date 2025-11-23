# FAA Based Queue

Implementation of a concurrent lock-free queue that uses fetch-and-add synchronization primitive.

This queue is based on a simulation of an infinite array for storing elements and manipulates enqIdx and deqIdx counters, which reference the next working cells in the array for enqueue and dequeue operations.
The "infinite" array is usually simulated via a linked list of fixed-size segments.

Related [paper](https://www.cs.tau.ac.il/~mad/publications/ppopp2013-x86queues.pdf)