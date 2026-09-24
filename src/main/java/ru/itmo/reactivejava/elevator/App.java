package ru.itmo.reactivejava.elevator;

import ru.itmo.reactivejava.elevator.benchmark.BenchmarkRunner;

import java.util.ArrayList;
import java.util.List;

public final class App {
    private static final List<Integer> DEFAULT_SIZES = List.of(5_000, 50_000, 250_000);

    private App() {
    }

    public static void main(String[] args) {
        boolean includeParallel = false;
        List<Integer> sizes = new ArrayList<>();

        for (String argument : args) {
            if ("--parallel".equals(argument)) {
                includeParallel = true;
            } else if ("--help".equals(argument) || "-h".equals(argument)) {
                printUsage();
                return;
            } else {
                sizes.add(parsePositiveSize(argument));
            }
        }

        if (sizes.isEmpty()) {
            sizes = DEFAULT_SIZES;
        }

        BenchmarkRunner.run(List.copyOf(sizes), includeParallel);
    }

    private static int parsePositiveSize(String argument) {
        final int size;
        try {
            size = Integer.parseInt(argument.replace("_", ""));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "unknown argument or invalid collection size: " + argument,
                    exception
            );
        }
        if (size <= 0) {
            throw new IllegalArgumentException("collection size must be positive: " + size);
        }
        return size;
    }

    private static void printUsage() {
        System.out.println("Usage: java ... App [--parallel] [size1 size2 ...]");
        System.out.println("Default sizes: 5000 50000 250000");
    }
}

