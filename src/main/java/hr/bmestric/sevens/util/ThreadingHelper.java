package hr.bmestric.sevens.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadingHelper {
    private static final Logger logger = LoggerFactory.getLogger(ThreadingHelper.class);

    private ThreadingHelper() {
        // Utility class
    }

    // Creates a fixed thread pool with daemon threads.
    public static ExecutorService createFixedThreadPool(int threadCount, String namePrefix) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be positive");
        }
        logger.debug("Creating fixed thread pool: {} threads with prefix '{}'", threadCount, namePrefix);
        return Executors.newFixedThreadPool(threadCount, r -> {
            Thread thread = new Thread(r, namePrefix + "-" + System.currentTimeMillis());
            thread.setDaemon(true);
            return thread;
        });
    }

    // Creates a single-threaded executor with daemon thread.
    public static ExecutorService createSingleThreadExecutor(String namePrefix) {
        logger.debug("Creating single thread executor with prefix '{}'", namePrefix);
        return Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, namePrefix);
            thread.setDaemon(true);
            return thread;
        });
    }

    // Creates a scheduled executor service for periodic tasks.
    public static ScheduledExecutorService createScheduledThreadPool(int threadCount, String namePrefix) {
        if (threadCount <= 0) {
            throw new IllegalArgumentException("Thread count must be positive");
        }
        logger.debug("Creating scheduled thread pool: {} threads with prefix '{}'", threadCount, namePrefix);
        return Executors.newScheduledThreadPool(threadCount, r -> {
            Thread thread = new Thread(r, namePrefix + "-Scheduled");
            thread.setDaemon(true);
            return thread;
        });
    }

    // Shuts down an executor service gracefully.
    public static void shutdownGracefully(ExecutorService executorService, int timeoutSeconds) {
        if (executorService == null || executorService.isShutdown()) {
            return;
        }

        logger.info("Shutting down executor service with {}s timeout", timeoutSeconds);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
                logger.warn("Executor did not terminate in time, forcing shutdown");
                executorService.shutdownNow();
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    logger.error("Executor did not terminate after forced shutdown");
                }
            } else {
                logger.info("Executor service shut down successfully");
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted while waiting for executor shutdown", e);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }



    //Runs a task on a daemon thread.
    public static void runOnDaemonThread(Runnable task, String threadName) {
        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
        logger.debug("Started daemon thread: {}", threadName);
    }
}
