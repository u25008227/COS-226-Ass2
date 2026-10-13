import java.util.concurrent.atomic.AtomicInteger;

public class Producer implements Runnable {
    private final MyQueue<Job> queue;
    private final int jobsToProduce;
    private final int producerId;
    private final AtomicInteger globalProducedCount;

    public Producer(MyQueue<Job> queue, int jobsToProduce, int producerId,
                    AtomicInteger globalProducedCount) {
        this.queue = queue;
        this.jobsToProduce = jobsToProduce;
        this.producerId = producerId;
        this.globalProducedCount = globalProducedCount;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < jobsToProduce; i++) {
                // Unique identifier combining producer id and job index
                int jobId = (producerId * 1000000) + i;
                Job job = new Job(jobId);
                queue.enq(job);
                globalProducedCount.incrementAndGet();
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    }
}