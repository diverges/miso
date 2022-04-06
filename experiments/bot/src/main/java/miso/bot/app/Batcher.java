package miso.bot.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javacord.api.entity.message.Message;

public class Batcher<Message> {
    private static Logger logger = Logger.getLogger(Batcher.class.getName());

    private final int MaxBatchSize = 100;
    private final ConcurrentLinkedQueue<Batch> freeBatches;
    private final ConcurrentLinkedQueue<Batch> readyBatches;
    private final AtomicReference<Batch> currentBatch;
    private final Object batchLock;

    public Batcher() {
        this.batchLock = new Object();
        this.currentBatch = new AtomicReference<>();
        this.readyBatches = new ConcurrentLinkedQueue<>();
        this.freeBatches = new ConcurrentLinkedQueue<>();
        freeBatches.addAll(Collections.nCopies(10, new Batch()));
    }

    /**
    * Buffer message to batch.
    */
    public void handleMessage(Message message) {
        var current = currentBatch.get();
        var size = current.size.incrementAndGet();
        if(size >= MaxBatchSize) {
            current = currentBatch.
        }

        // Add to the batch
    }

    /**
     * Builds and dequeues the next batch to process.
     * @return
     */
    public Iterable<Message> getBatch() {
        var current = this.readyBatches.poll();
        if(current == null) {
            current = this.currentBatch.getAndSet(freeBatches.poll());
        }

        if(current.size.get() == 0) return null;

        // Ensure no pending operations on the current batch.
        synchronized(current.lock) {
            var result = new ArrayList<>(current.batch);
            current.batch.clear();
            current.size.set(0);
            freeBatches.add(current);
            return result;
        }
    }

    // Batch object which contains iterable of messages
    // messages arrive and are placed on the enxt ready batch
    private class Batch {
        final Object lock = new Object();
        AtomicInteger size;
        ArrayList<Message> batch;
    }
}
