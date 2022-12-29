package miso.bot.app.core;

import java.lang.Thread;

public abstract class BatchProcessor<E> extends Thread {

    private Batcher<E> batcher;

    public BatchProcessor(Batcher<E> batcher) {
        this.batcher = batcher;
    }

    public abstract void process(Batcher<E>.Batch batch);

    @Override
    public void run() {
        Batcher<E>.Batch batch = null;
        while(true) {
            batch = batcher.getBatch();
            try {
                if(batch != null) {
                    this.process(batch);
                    continue;
                }

                if(Thread.currentThread().isInterrupted()) {
                    return;
                }

                Thread.sleep(250);
            } catch (InterruptedException ex) {
                // Re-interrupt, allowing the this thread to completely flush
                // the remaining batches before terminating.
                interrupt();
            } finally {
                if(batch != null) batch.complete();
            }
        }
    }
}