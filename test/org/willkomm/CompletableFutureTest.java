package org.willkomm;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CompletableFutureTest {
    public class Fubar extends RuntimeException {}

    @Test
    public void intermediateStageHandlesException_givenNoException() {
        CompletableFuture<String> f = CompletableFuture.supplyAsync(
                () -> "lets go!"
        ).handleAsync(
                (result, reason) -> {
                    if (reason instanceof Fubar) {
                        return "boo";
                    }

                    return result;
                }
        );

        assertEquals("lets go!", f.join());
    }

    @Test
    public void intermediateStageHandlesException_givenAnException() {
        CompletableFuture<String> f = CompletableFuture.supplyAsync(
                () -> {
                    if (false) {
                        return "lets go!";
                    } else {
                        throw new Fubar();
                    }
                }
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
}
