public class Consumer implements Runnable {
    private final MyQueue<Job> queue;
    private final int jobsToConsume;
    private final java.util.concurrent.atomic.AtomicInteger globalProcessedCount;

    public Consumer(MyQueue<Job> queue, int jobsToConsume, java.util.concurrent.atomic.AtomicInteger globalProcessedCount) {
        this.queue = queue;
        this.jobsToConsume = jobsToConsume;
        this.globalProcessedCount = globalProcessedCount;
    }

    @Override
    public void run() {
        int consumed = 0;
        while (consumed < jobsToConsume) {
            Job job = queue.deq();
            if (job != null) {
                // Processing a job means successfully removing it and recording it
                globalProcessedCount.incrementAndGet();
                consumed++;
            } else {
                // Yield or briefly pause if queue is temporarily empty to prevent tight-loop CPU spinning
                Thread.yield();
            }
        }
    }
}