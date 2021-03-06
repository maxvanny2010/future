package com.example.completablefuture;

import java.util.List;

public class RealLifeWithoutCompletableFutureExample {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        List<Car> cars = cars();
        cars.forEach(car -> {
            float rating = rating(car.manufacturerId);
            car.setRating(rating);
        });

        cars.forEach(System.out::println);

        long end = System.currentTimeMillis();

        System.out.println("Took " + (end - start) + " ms.");
    }

    static float rating(int manufacturer) {
        try {
            simulateDelay();
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
    }

    static List<Car> cars() {
        return List.of(
                new Car(1, 3, "Fiesta", 2017),
                new Car(2, 7, "Camry", 2014),
                new Car(3, 2, "M2", 2008));
    }

    private static void simulateDelay() throws InterruptedException {
        Thread.sleep(5000);
    }
}

