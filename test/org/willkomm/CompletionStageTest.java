package org.willkomm;

import org.junit.jupiter.api.*;

import java.text.*;
import java.util.concurrent.*;
import java.util.logging.*;

import static org.junit.jupiter.api.Assertions.*;

public class CompletionStageTest {
    private ArrayBlockingQueue<Integer> queue;
    private Logger logger;

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        Formatter formatter = new Formatter() {
            @Override
            public String format(LogRecord record) {
                return MessageFormat.format("[thread: {0}, method: {1}] {2}\n", record.getThreadID(), record.getSourceMethodName(), record.getMessage());
            }
        };

        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(formatter);
        handler.setLevel(Level.ALL);

        logger = Logger.getAnonymousLogger();
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);

        queue = new ArrayBlockingQueue<>(10);

        logger.fine(testInfo.getDisplayName());
    }

    @AfterEach
    public void afterEach() {
        System.out.println();
        System.out.println();
    }

    @Test
    public void intermediateStageWaitsForProceedingStage() {
        CompletableFuture.supplyAsync(() -> {
            System.out.println("stage1");
            return "stage1";
        }).thenApplyAsync(s -> {
            System.out.println("stage2");
            return "stage2";
        });
    }

    @Test
    public void exploration() throws InterruptedException {
        CompletionStage<Void> s2 = createCompletionStage(2);
//        Thread.sleep(1);

        createCompletionStage(1)
                .thenCompose(s -> s2)
                .thenCompose(s -> createCompletionStage(3))
                .toCompletableFuture()
                .join();

        assertEquals("[2, 1, 3]", queue.toString());
    }

    @Test
    public void completableFutureReturnedFromMethodDoesNotStartUntilAfterPreviousStageFinishesWhenComposed() {
        createCompletionStage(1)
                .thenCompose(s -> createCompletionStage(2))
                .toCompletableFuture()
                .join();

        assertEquals("[1, 2]", queue.toString());
    }

    @Test
    public void ex3() {
        logger.fine("");

        createCompletionStage(1)
                .thenApplyAsync(s -> {
                    logger.fine("queue.add(2)");
                    queue.add(2);
                    return null;
                })
                .thenCompose(s -> createCompletionStage(3))
                .toCompletableFuture()
                .join();

        assertEquals("[1, 2, 3]", queue.toString());
    }

    private CompletionStage<Void> createCompletionStage(int currentStage) {
        logger.fine("createCompletionStage(" + currentStage +")");

        return CompletableFuture.supplyAsync(() -> {
            logger.fine("queue.add(" + currentStage + ")");
            queue.add(currentStage);
            return null;
        });
    }
}
