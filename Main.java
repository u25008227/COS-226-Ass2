import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    //TASK 3: "Same total workload for every experiment"
    // The spec requires the total number of jobs to remain constant
    // across all configurations and both queue implementations.
    static final int TOTAL_JOBS = 12000;

    //TASK 3: "Same capacity must be used when comparing runs"
    // Fixed capacity for the bounded queue across every experiment.
    static final int CAPACITY = 100;

    //TASK 3: "You must test the following configurations"
    // {4,4} = balanced, {6,2} = producer-heavy, {2,6} = consumer-heavy
    static final int[][] CONFIGS = { {4, 4}, {6, 2}, {2, 6} };

    //TASK 3: "The program must run using either queue"
    // Factory that lets us swap between bounded and lock-free queues
    // so the same producer/consumer code works with both.
    static MyQueue<Job> createQueue(String type) {
        switch (type) {
            case "bounded":
                return new BoundedQueue<>(CAPACITY);
            case "lockfree":
                return new LockFreeQueue<>();
            default:
                throw new IllegalArgumentException("Unknown queue type: " + type);
        }
    }

    //TASK 3: "Runs one experiment and records results"
    // Each call runs ONE (queue, P, C) combination and returns the
    // total execution time in milliseconds.
    static long runExperiment(String queueType, int numProducers, int numConsumers)
            throws InterruptedException {

        //TASK 3: "The work should be divided between producer threads"
        // e.g. 12000 / 4 = 3000 jobs each, 12000 / 6 = 2000 each, etc.
        if (TOTAL_JOBS % numProducers != 0 || TOTAL_JOBS % numConsumers != 0) {
            throw new IllegalArgumentException(
                "TOTAL_JOBS must divide evenly by the number of producers and consumers");
        }

        int jobsPerProducer = TOTAL_JOBS / numProducers;
        int jobsPerConsumer = TOTAL_JOBS / numConsumers;

        //TASK 3: "Demonstrate that all produced jobs are correctly processed"
        // Atomic counters record totals so we can verify produced == processed == TOTAL_JOBS.
        AtomicInteger producedCount = new AtomicInteger(0);
        AtomicInteger processedCount = new AtomicInteger(0);

        //TASK 3: "Creates the selected queue implementation"
        MyQueue<Job> queue = createQueue(queueType);

        Thread[] producers = new Thread[numProducers];
        Thread[] consumers = new Thread[numConsumers];

        //TASK 3: "Record the total execution time"
        // Start the clock just before threads launch.
        long startTime = System.nanoTime();

        //TASK 3: "Creates and starts the required number of producer threads"
        for (int i = 0; i < numProducers; i++) {
            producers[i] = new Thread(new Producer(queue, jobsPerProducer, i, producedCount));
            producers[i].start();
        }

        //TASK 3: "Creates and starts the required number of consumer threads"
        for (int i = 0; i < numConsumers; i++) {
            consumers[i] = new Thread(new Consumer(queue, jobsPerConsumer, processedCount));
            consumers[i].start();
        }

        //TASK 3: "Waits for all work to be completed"
        for (Thread p : producers) p.join();
        for (Thread c : consumers) c.join();

        //TASK 3: "Total execution time"
        long durationMs = (System.nanoTime() - startTime) / 1_000_000;

        //TASK 3: "Displays enough information to demonstrate correctness"
        boolean ok = producedCount.get() == TOTAL_JOBS && processedCount.get() == TOTAL_JOBS;
        System.out.println("[" + queueType + "] " + numProducers + "P/" + numConsumers + "C"
                + " | produced: " + producedCount.get()
                + " | processed: " + processedCount.get()
                + " / " + TOTAL_JOBS
                + " | " + (ok ? "OK" : "MISMATCH!"));

        return durationMs;
    }

    public static void main(String[] args) throws InterruptedException {

        //TASK 3: "Runs using either the bounded queue or the lock-free queue"
        // Default: run BOTH so we get a full comparison table in one go.
        String[] queueTypes = args.length > 0 ? args
                : new String[] { "bounded", "lockfree" };

        System.out.println("Total workload: " + TOTAL_JOBS + " jobs per experiment.\n");

        // TASK 3: "Present your results in a table"
        // Columns required by the spec: queue, producers, consumers, time, throughput.
        StringBuilder table = new StringBuilder();
        table.append(String.format("%-10s %-10s %-10s %-14s %-18s%n",
                "Queue", "Producers", "Consumers", "Time (ms)", "Throughput (jobs/s)"));
        table.append("-".repeat(70)).append("\n");

        //TASK 3: Run every queue type against every configuration
        for (String queueType : queueTypes) {
            for (int[] config : CONFIGS) {
                long duration = runExperiment(queueType, config[0], config[1]);

                //TASK 3: "Throughput i.e. number of jobs processed per second"
                double throughput = TOTAL_JOBS / (Math.max(duration, 1) / 1000.0);

                //TASK 3: "Record: queue, producers, consumers, time, throughput"
                table.append(String.format("%-10s %-10d %-10d %-14d %-18.2f%n",
                        queueType, config[0], config[1], duration, throughput));
            }
        }

        //TASK 3: "Present your results in a table"
        System.out.println("\n===== RESULTS TABLE =====");
        System.out.println(table);
    }
}