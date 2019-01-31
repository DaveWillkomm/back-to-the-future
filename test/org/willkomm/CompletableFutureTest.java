package org.willkomm;

import org.junit.jupiter.api.*;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class CompletableFutureTest {
    public class Fubar extends RuntimeException {}

    public String goOrFail(boolean go) {
        if (go) {
            return "Let's go!";
        }
        throw new Fubar();
    }

    @Test
    public void intermediateStageHandlesException_givenNoException() {
        CompletableFuture<String> f = CompletableFuture.supplyAsync(
                () -> goOrFail(true)
        ).handleAsync(
                (result, reason) -> {
                    if (reason instanceof Fubar) {
                        return "boo";
                    }

                    return result;
                }
        );

        assertEquals("Let's go!", f.join());
    }

    @Test
    public void intermediateStageHandlesException_givenAnException() {
        CompletableFuture<String> f = CompletableFuture.supplyAsync(
                () -> goOrFail(false)
        ).handleAsync(
                (result, reason) -> {
                    if (reason.getCause() instanceof Fubar) {
                        return "boo";
                    }

                    return result;
                }
        );

        assertEquals("boo", f.join());
    }

    @Test
    public void intermediateStageIgnoresException_givenNoException() {
        CompletableFuture<String> f = CompletableFuture.supplyAsync(
                () -> goOrFail(true)
        ).thenApplyAsync(
                result -> result
        );

        assertEquals("Let's go!", f.join());
    }

    @Test
    public void intermediateStageIgnoresException_givenAnException() {
        Throwable t = assertThrows(
                ExecutionException.class,
                () -> CompletableFuture.supplyAsync(
                        () -> goOrFail(false)
                ).thenApplyAsync(
                        (result) -> result
                ).get()
        );
        assertEquals(t.getCause().getClass(), Fubar.class);
    }
}
