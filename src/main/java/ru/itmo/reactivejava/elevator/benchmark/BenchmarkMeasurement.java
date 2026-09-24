package ru.itmo.reactivejava.elevator.benchmark;

import ru.itmo.reactivejava.elevator.statistics.FleetStatistics;

import java.util.Objects;

public record BenchmarkMeasurement(
        CalculationMethod method,
        int elementCount,
        long startedNanos,
        long finishedNanos,
        FleetStatistics result
) {
    public BenchmarkMeasurement {
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(result, "result");
        if (elementCount < 0) {
            throw new IllegalArgumentException("elementCount must not be negative");
        }
        if (finishedNanos - startedNanos < 0) {
            throw new IllegalArgumentException("finishedNanos precedes startedNanos");
        }
    }

    public long durationNanos() {
        return finishedNanos - startedNanos;
    }

    public double durationMillis() {
        return durationNanos() / 1_000_000.0;
    }
}

