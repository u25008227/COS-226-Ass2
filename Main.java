import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    static final int TOTAL_JOBS = 12000;
    static final int CAPACITY = 100; // same capacity for every bounded-queue run

    // {producers, consumers}: balanced, producer-heavy, consumer-heavy
    static final int[][] CONFIGS = { {4, 4}, {6, 2}, {2, 6} };

    static MyQueue<Job> createQueue(String type) {
        switch (type) {
            case "bounded":
                return new BoundedQueue<>(CAPACITY);
            // Uncomment once your LockFreeQueue exists:
            // case "lockfree":
            //     return new LockFreeQueue<>();
            default:
                throw new IllegalArgumentException("Unknown queue type: " + type);
        }
    }

    // Runs one experiment and returns {durationMs}
    static long runExperiment(String queueType, int numProducers, int numConsumers)
            throws InterruptedException {

        // Guard against silently losing jobs through integer division
        if (TOTAL_JOBS % numProducers != 0 || TOTAL_JOBS % numConsumers != 0) {
            throw new IllegalArgumentException(
                "TOTAL_JOBS must divide evenly by the number of producers and consumers");
        }

        int jobsPerProducer = TOTAL_JOBS / numProducers;
        int jobsPerConsumer = TOTAL_JOBS / numConsumers;

        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger processedCount = new AtomicInteger(0);

        MyQueue<Job> queue = createQueue(queueType);

        Thread[] producers = new Thread[numProducers];
        Thread[] consumers = new Thread[numConsumers];

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numProducers; i++) {
            producers[i] = new Thread(new Producer(queue, jobsPerProducer, i, producedCount));
            producers[i].start();
        }

        for (int i = 0; i < numConsumers; i++) {
            consumers[i] = new Thread(new Consumer(queue, jobsPerConsumer, processedCount));
            consumers[i].start();
        }

        for (Thread p : producers) p.join();
        for (Thread c : consumers) c.join();

        long duration = System.currentTimeMillis() - startTime;

        boolean ok = producedCount.get() == TOTAL_JOBS && processedCount.get() == TOTAL_JOBS;
        System.out.println("[" + queueType + "] " + numProducers + "P/" + numConsumers + "C"
                + " | produced: " + producedCount.get()
                + " | processed: " + processedCount.get()
                + " / " + TOTAL_JOBS
                + " | " + (ok ? "OK" : "MISMATCH!"));

        return duration;
    }

    public static void main(String[] args) throws InterruptedException {
        // Usage: java Main [bounded] [lockfree]   (defaults to bounded)
        String[] queueTypes = args.length > 0 ? args : new String[] { "bounded" };

        System.out.println("Total workload: " + TOTAL_JOBS + " jobs per experiment.\n");

        StringBuilder table = new StringBuilder();
        table.append(String.format("%-10s %-10s %-10s %-14s %-18s%n",
                "Queue", "Producers", "Consumers", "Time (ms)", "Throughput (jobs/s)"));

        for (String queueType : queueTypes) {
            for (int[] config : CONFIGS) {
                long duration = runExperiment(queueType, config[0], config[1]);
                double throughput = TOTAL_JOBS / (Math.max(duration, 1) / 1000.0);
                table.append(String.format("%-10s %-10d %-10d %-14d %-18.2f%n",
                        queueType, config[0], config[1], duration, throughput));
            }
        }

        System.out.println("\nResults:");
        System.out.println(table);
    }
}