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
    public void stage312() throws InterruptedException {
        CompletionStage<Integer> stage3 = createCompletionStage(3);
        CompletionStage<Integer> stage1 = createCompletionStage(1);
        CompletionStage<Integer> stage2 = createCompletionStage(2);

        // Without this pause, the CompletableFuture created async stages will execute in different orders depending upon how many threads are used.
        Thread.sleep(1);

        CompletionStage<Integer> chain = stage1
                .thenCompose(x -> stage2)
                .thenCompose(x -> stage3);

        assertEquals(3, queue.remove().intValue());
        assertEquals(1, queue.remove().intValue());
        assertEquals(2, queue.remove().intValue());

        assertEquals(3, chain.toCompletableFuture().join().intValue());
    }

    @Test
    public void stage213() {
        CompletionStage<Integer> s2 = createCompletionStage(2);

        CompletionStage<Integer> chain = createCompletionStage(1)
                .thenCompose(s -> s2)
                .thenCompose(s -> createCompletionStage(3));

        // If this is commented out, the final assertion below may fail as it's stage hasn't executed yet.
        assertEquals(3, chain.toCompletableFuture().join().intValue());

        assertEquals(2, queue.remove().intValue());
        assertEquals(1, queue.remove().intValue());
        assertEquals(3, queue.remove().intValue());
    }

    @Test
    public void stage123() {
        CompletionStage<Integer> chain = createCompletionStage(1)
                .thenCompose(s -> createCompletionStage(2))
                .thenCompose(s -> createCompletionStage(3));

        // If this is commented out, the final assertion below may fail as it's stage hasn't executed yet.
        assertEquals(3, chain.toCompletableFuture().join().intValue());

        assertEquals(1, queue.remove().intValue());
        assertEquals(2, queue.remove().intValue());
        assertEquals(3, queue.remove().intValue());
    }

    @Test
    public void stage123_given2IsInline() {
        createCompletionStage(1)
                .thenApplyAsync(s -> {
                    logger.fine("queue.add(2)");
                    queue.add(2);
                    return null;
                })
                .thenCompose(s -> createCompletionStage(3))
                .toCompletableFuture()
                .join();

        assertEquals(1, queue.remove().intValue());
        assertEquals(2, queue.remove().intValue());
        assertEquals(3, queue.remove().intValue());
    }

    private CompletionStage<Integer> createCompletionStage(int currentStage) {
        logger.fine("createCompletionStage(" + currentStage +")");

        return CompletableFuture.supplyAsync(() -> {
            logger.fine("queue.add(" + currentStage + ")");
            queue.add(currentStage);
            return currentStage;
        });
    }
}
