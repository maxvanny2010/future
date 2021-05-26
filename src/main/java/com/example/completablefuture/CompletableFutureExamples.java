package com.example.completablefuture;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class CompletableFutureExamples {

    static ExecutorService executor = Executors.newFixedThreadPool(3, new ThreadFactory() {
        int count = 1;

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "custom-executor-" + count++);
        }
    });

    static Random random = new Random();

    public static void main(String[] args) {
        try {
//            allOfAsyncExample();
            //  completedFutureExample();
            //  completeExceptionallyExample();
            //  runAsyncExample();
            // thenApplyExample();
            //  thenApplyAsyncExample();
            //  thenApplyAsyncWithExecutorExample();
            // thenAcceptExample();
            //  thenAcceptAsyncExample();
            // applyToEitherExample();
            // cancelExample();
            //  applyToEitherExample();
            // acceptEitherExample();
            //  runAfterBothExample();
            //  thenAcceptBothExample();
            //  thenCombineExample();
            //  thenComposeExample();
            // anyOfExample();
            // allOfExample();
            //  allOfAsyncExample();
        } finally {
            executor.shutdown();
        }
    }

    static void completedFutureExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message");
        // System.out.println(cf.getNow(null));
        assertEquals("message", cf.getNow(null));
    }

    static void completeExceptionallyExample() {
        CompletableFuture<String> cf = CompletableFuture
                .completedFuture("message")
                .thenApplyAsync(String::toUpperCase, CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        //  System.out.println(cf.getNow(null));
        CompletableFuture<String> exceptionHandler = cf.handle((s, th) -> (th != null) ? "message upon cancel" : "");
        //   System.out.println(cf.getNow(null));
        cf.completeExceptionally(new RuntimeException("completed exceptionally"));
        assertTrue("Was not completed exceptionally", cf.isCompletedExceptionally());
        try {
            cf.join();
            fail("Should have thrown an exception");
        } catch (CompletionException ex) { // just for testing
            assertEquals("completed exceptionally", ex.getCause().getMessage());
        }

        assertEquals("message upon cancel", exceptionHandler.join());
    }

    static void runAsyncExample() {
        CompletableFuture<Void> cf = CompletableFuture.runAsync(() -> {
            // System.out.println(Thread.currentThread().isDaemon());
            assertTrue(Thread.currentThread().isDaemon());
            randomSleep();
        });
        assertFalse(cf.isDone());
        sleepEnough();
        assertTrue(cf.isDone());
    }

    static void thenApplyExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApply(String::toUpperCase);
        assertEquals("MESSAGE", cf.getNow(null));
        System.out.println(cf.getNow(null));
    }

    static void thenApplyAsyncExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            randomSleep();
            return s.toUpperCase();
        });
        assertNull(cf.getNow(null));
        System.out.println(cf.getNow(null));
        assertEquals("MESSAGE", cf.join());
        System.out.println(cf.getNow(null));
    }

    static void thenApplyAsyncWithExecutorExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(s -> {
            assertTrue(Thread.currentThread().getName().startsWith("custom-executor-"));
            assertFalse(Thread.currentThread().isDaemon());
            randomSleep();
            return s.toUpperCase();
        }, executor);

        assertNull(cf.getNow(null));
        System.out.println(cf.getNow(null));
        assertEquals("MESSAGE", cf.join());
        System.out.println(cf.getNow(null));
    }

    static void thenAcceptExample() {
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture("thenAccept message")
                .thenAccept(result::append);
        assertTrue("Result was empty", result.length() > 0);
        System.out.println(result.toString());
    }

    static void thenAcceptAsyncExample() {
        StringBuilder result = new StringBuilder();
        CompletableFuture<Void> cf = CompletableFuture.completedFuture("thenAcceptAsync message")
                .thenAcceptAsync(result::append);
        cf.join();
        System.out.println(cf.getNow(null));
        assertTrue("Result was empty", result.length() > 0);
    }

    static void cancelExample() {
        CompletableFuture<String> cf = CompletableFuture.completedFuture("message").thenApplyAsync(String::toUpperCase,
                CompletableFuture.delayedExecutor(1, TimeUnit.SECONDS));
        CompletableFuture<String> cf2 = cf.exceptionally(throwable -> "canceled message");
        System.out.println(cf.getNow(null));
        assertTrue("Was not canceled", cf.cancel(true));
        System.out.println(cf.getNow(null));
        assertTrue("Was not completed exceptionally", cf.isCompletedExceptionally());
        assertEquals("canceled message", cf2.join());
        System.out.println(cf.getNow(null));
    }

    static void applyToEitherExample() {
        String original = "Message";
        CompletableFuture<String> cf1 = CompletableFuture.completedFuture(original)
                .thenApplyAsync(CompletableFutureExamples::delayedUpperCase);
        CompletableFuture<String> cf2 = cf1.applyToEither(
                CompletableFuture.completedFuture(original)
                        .thenApplyAsync(CompletableFutureExamples::delayedLowerCase),
                s -> s + " from applyToEither");
        assertTrue(cf2.join().endsWith(" from applyToEither"));
        System.out.println(cf2.getNow(null));
    }

    static void acceptEitherExample() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture<Void> cf = CompletableFuture.completedFuture(original)
                .thenApplyAsync(CompletableFutureExamples::delayedUpperCase)
                .acceptEither(CompletableFuture.completedFuture(original)
                                .thenApplyAsync(CompletableFutureExamples::delayedLowerCase),
                        s -> result.append(s).append("acceptEither"));
        cf.join();
        System.out.println(result.toString());
        assertTrue("Result was empty", result.toString().endsWith("acceptEither"));
    }

    static void runAfterBothExample() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).runAfterBoth(
                CompletableFuture.completedFuture(original).thenApply(String::toLowerCase),
                () -> result.append("done"));
        System.out.println(result.toString());
        assertTrue("Result was empty", result.length() > 0);
    }

    static void thenAcceptBothExample() {
        String original = "Message";
        StringBuilder result = new StringBuilder();
        CompletableFuture.completedFuture(original).thenApply(String::toUpperCase).thenAcceptBoth(
                CompletableFuture.completedFuture(original).thenApply(String::toLowerCase),
                (s1, s2) -> result.append(s1).append(s2));
        System.out.println(result.toString());
        assertEquals("MESSAGEmessage", result.toString());
    }

    static void thenCombineExample() {
        String original = "Message";
        CompletableFuture<String> cf = CompletableFuture.completedFuture(original)
                .thenApply(CompletableFutureExamples::delayedUpperCase)
                .thenCombine(CompletableFuture.completedFuture(original)
                                .thenApply(CompletableFutureExamples::delayedLowerCase),
                        (s1, s2) -> s1 + s2);
        System.out.println(cf.getNow(null));
        assertEquals("MESSAGEmessage", cf.getNow(null));
    }

    static void thenCombineAsyncExample() {
        String original = "Message";
        CompletableFuture<String> cf = CompletableFuture.completedFuture(original)
                .thenApplyAsync(CompletableFutureExamples::delayedUpperCase)
                .thenCombine(CompletableFuture.completedFuture(original)
                                .thenApplyAsync(CompletableFutureExamples::delayedLowerCase),
                        (s1, s2) -> s1 + s2);
        System.out.println(cf.join());
        //  assertEquals("MESSAGEmessage", cf.join());
    }

    static void thenComposeExample() {
        String original = "Message";
        CompletableFuture<String> cf = CompletableFuture.completedFuture(original)
                .thenApply(CompletableFutureExamples::delayedUpperCase)
                .thenCompose(upper -> CompletableFuture.completedFuture(original)
                        .thenApply(CompletableFutureExamples::delayedLowerCase)
                        .thenApply(s -> upper + s));
        System.out.println(cf.getNow(null));
        assertEquals("MESSAGEmessage", cf.join());
    }

    static void anyOfExample() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        CompletableFuture.anyOf(messages.stream()
                .map(msg -> CompletableFuture.completedFuture(msg)
                        .thenApply(CompletableFutureExamples::delayedUpperCase))
                .toArray(CompletableFuture[]::new)).whenComplete((res, th) -> {
            if (th == null) {
                assertTrue(isUpperCase((String) res));
                result.append(res);
            }
        });
        System.out.println(result.toString());
        assertTrue("Result was empty", result.length() > 0);
    }

    static void allOfExample() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream()
                .map(msg -> CompletableFuture.completedFuture(msg).thenApply(CompletableFutureExamples::delayedUpperCase))
                .collect(toList());
        //if all are completed. return 3 true.
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).whenComplete((v, th) -> {
            futures.forEach(cf -> System.out.println(isUpperCase(cf.join())));
            result.append("done");
        });
        futures.forEach(cf -> System.out.println(cf.join()));
        System.out.println(result.toString());
        assertTrue("Result was empty", result.length() > 0);
    }

    static void allOfAsyncExample() {
        StringBuilder result = new StringBuilder();
        List<String> messages = Arrays.asList("a", "b", "c");
        List<CompletableFuture<String>> futures = messages.stream()
                .map(msg -> CompletableFuture.completedFuture(msg).thenApplyAsync(CompletableFutureExamples::delayedUpperCase))
                .collect(toList());
        CompletableFuture<Void> allOf = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((v, th) -> {
                    futures.forEach(cf -> assertTrue(isUpperCase(cf.getNow(null))));
                    result.append("done");
                });
        // System.out.println(result.toString());
        allOf.join();
        System.out.println(result.toString());
        assertTrue("Result was empty", result.length() > 0);
    }

    private static boolean isUpperCase(String s) {
        return IntStream.range(0, s.length()).noneMatch(i -> Character.isLowerCase(s.charAt(i)));
    }

    private static String delayedUpperCase(String s) {
        randomSleep();
        return s.toUpperCase();
    }

    private static String delayedLowerCase(String s) {
        randomSleep();
        return s.toLowerCase();
    }

    private static void randomSleep() {
        try {
            Thread.sleep(random.nextInt(1000));
        } catch (InterruptedException e) {
            // ...
        }
    }

    private static void sleepEnough() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // ...
        }
    }

}
