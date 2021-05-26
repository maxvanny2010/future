package com.example.completablefuture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.stream.Collectors.toList;

public class RealLifeCompletableFutureExample {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        cars().thenCompose(cars -> {
            List<CompletionStage<Car>> updatedCars = cars
                    .stream()
                    .map(car -> rating(car.manufacturerId)
                            .thenApply(r -> {
                                car.setRating(r);
                                return car;
                            })).collect(toList());

            CompletableFuture<Void> done = CompletableFuture
                    .allOf(updatedCars.toArray(CompletableFuture[]::new));
            return done.thenApply(v -> updatedCars
                    .stream()
                    .map(CompletionStage::toCompletableFuture)
                    .map(CompletableFuture::join)
                    .collect(toList()));
        }).whenComplete((cars, th) -> {
            if (th == null) {
                cars.forEach(System.out::println);
            } else {
                throw new RuntimeException(th);
            }
        }).toCompletableFuture().join();

        long end = System.currentTimeMillis();

        System.out.println("Took " + (end - start) + " ms.");
    }

    static CompletionStage<Float> rating(int manufacturer) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                simulateDelay();
                System.out.println("DElAY");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            return switch (manufacturer) {
                case 2 -> 4f;
                case 3 -> 4.1f;
                case 7 -> 4.2f;
                default -> 5f;
            };
        }).exceptionally(th -> -1f);
    }

    static CompletionStage<List<Car>> cars() {
        final var carList = List.of(
                new Car(1, 3, "Fiesta", 2017),
                new Car(2, 7, "Camry", 2014),
                new Car(3, 2, "M2", 2008));
        return CompletableFuture.supplyAsync(() -> carList);
    }

    private static void simulateDelay() throws InterruptedException {
        Thread.sleep(5000);
    }
}
