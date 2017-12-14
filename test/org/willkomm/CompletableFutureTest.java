package org.willkomm;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                (result) -> result
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
