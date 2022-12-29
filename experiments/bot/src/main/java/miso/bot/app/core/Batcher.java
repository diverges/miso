package miso.bot.app.core;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Batcher<E> {
    private static Logger logger = Logger.getLogger(Batcher.class.getName());

    private final int maxBatchSize = 10;
    private final ConcurrentLinkedQueue<Batch> freeBatches;
    private final ConcurrentLinkedQueue<Batch> readyBatches;
    private final AtomicReference<Batch> currentBatch;
    private final Object swapLock;

    public Batcher() {
        this.swapLock = new Object();
        this.currentBatch = new AtomicReference<>();
        this.readyBatches = new ConcurrentLinkedQueue<>();
        this.freeBatches = new ConcurrentLinkedQueue<>();
        for(int i = 0; i < 10; ++i) {
            freeBatches.add(new Batch(this));
        }
    }

    /**
    * Messages received are appended to the next ready batch with
    * the maximum batch size loosely enforced.
    */
    public boolean handleMessage(E message) {
        Batch current = null;
        while(true) {
            current = currentBatch.get();
            if(current == null) {
                // No available batches, which occurs
                // when all batches are currently being
                // consumed.
                var next = this.freeBatches.poll();
                if(next == null) return false;

                // Update the current batch and attempt the get()
                // until there is no current and free batches.
                if(!this.currentBatch.compareAndSet(null, next)){
                    this.freeBatches.add(next);
                }
            } else {
                break;
            }
        }

        var size = current.size.incrementAndGet();
        current.items.add(message);
        logger.log(Level.INFO, "Added message to batch of size={0}", size);

        if(size >= maxBatchSize) {
            // Ensure the current batch is only added to
            // the ready batches once.
            synchronized (this.swapLock) {
                if(currentBatch.get() != current) {
                    return true;
                }

                logger.log(Level.INFO, "Ready batch of size={0}", size);
                this.readyBatches.add(current);
                var next = this.freeBatches.poll();
                if(!this.currentBatch.compareAndSet(current, next)){
                    this.freeBatches.add(next);
                }
            }
        }

        return true;
    }

    /**
     * Builds and dequeues the next batch to process.
     * @return
     */
    public Batch getBatch() {
        while(true) {
            var current = this.readyBatches.poll();
            if(current != null) {
                return current;
            }

            // Check if the batch being prepared has
            // any items.
            current = this.currentBatch.get();
    
            // Nothing to send.
            if(current == null || current.size.get() == 0) {
                return null;
            }

            var next = this.freeBatches.poll();
            var witnessed = this.currentBatch.compareAndExchange(current, next);
            if(current == witnessed) {
                return current;
            }
            this.freeBatches.add(next);
        }
    }

    /**
     * Batch contains messages ready to be processed.
     * Once processed, complete() must be called to
     * return the batch to its respetive Batcher.
     */
    public class Batch {
        private final Batcher<E> parent;
        private final AtomicInteger size;
        private final Vector<E> items;

        private Batch(Batcher<E> parent) {
            this.parent = parent;
            this.size = new AtomicInteger(0);
            this.items = new Vector<>(maxBatchSize);
        }

        /**
         * @return the items
         */
        public List<E> getItems() {
            return items;
        }

        public void complete() {
            // Completes processing the current batch,
            // enqueues it onto the batcher's free batch pool.
            if(parent == null) {
                return;
            }

            size.set(0);
            items.clear();
            if(!parent.currentBatch.compareAndSet(null, this)) {
                parent.freeBatches.add(this);
            }
        }
    }
}
