package org.willkomm;

import org.junit.jupiter.api.*;

import java.util.concurrent.*;

public class CompletionStageTest {
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
        CompletionStage<String> s2 = createCompletionStage("stage2");
        Thread.sleep(1);
        CompletionStage<String> s1 = createCompletionStage("stage1");

        s1.thenCompose(s -> s2)
                .thenCompose(s -> createCompletionStage("stage3"));
    }

    @Test
    public void ex2() {
        createCompletionStage("stage1")
                .thenCompose(s -> createCompletionStage("stage2"));
    }

    @Test
    public void ex3() {
        createCompletionStage("stage1")
                .thenApplyAsync(s -> {
                    System.out.println("stage2");
                    return "stage2";
                })
                .thenCompose(s -> createCompletionStage("stage3"));
    }

    private CompletionStage<String> createCompletionStage(String value) {
        System.out.println("createCompletionStage(" + value + ")");

        return CompletableFuture.supplyAsync(() -> {
            System.out.println(value);

            return value;
        });
    }
}
