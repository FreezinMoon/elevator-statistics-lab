package ru.itmo.reactivejava.elevator.statistics;

import java.util.DoubleSummaryStatistics;

public record LoadStatistics(
        long eventCount,
        double minLoadKg,
        double averageLoadKg,
        double maxLoadKg,
        double totalLoadKg
) {
    public LoadStatistics {
        if (eventCount < 0) {
            throw new IllegalArgumentException("eventCount must not be negative");
        }
        if (!allFinite(minLoadKg, averageLoadKg, maxLoadKg, totalLoadKg)) {
            throw new IllegalArgumentException("load statistics must be finite");
        }
        if (minLoadKg < 0.0 || averageLoadKg < 0.0
                || maxLoadKg < 0.0 || totalLoadKg < 0.0) {
            throw new IllegalArgumentException("load statistics must not be negative");
        }
        if (eventCount == 0
                && (minLoadKg != 0.0 || averageLoadKg != 0.0
                || maxLoadKg != 0.0 || totalLoadKg != 0.0)) {
            throw new IllegalArgumentException("empty statistics must contain zero values");
        }
        if (eventCount > 0 && (minLoadKg > averageLoadKg || averageLoadKg > maxLoadKg)) {
            throw new IllegalArgumentException("expected min <= average <= max");
        }
    }

    public static LoadStatistics from(DoubleSummaryStatistics statistics) {
        if (statistics.getCount() == 0) {
            return empty();
        }
        return new LoadStatistics(
                statistics.getCount(),
                statistics.getMin(),
                statistics.getAverage(),
                statistics.getMax(),
                statistics.getSum()
        );
    }

    public static LoadStatistics empty() {
        return new LoadStatistics(0, 0.0, 0.0, 0.0, 0.0);
    }

    public boolean approximatelyEquals(LoadStatistics other, double tolerance) {
        return other != null
                && eventCount == other.eventCount
                && close(minLoadKg, other.minLoadKg, tolerance)
                && close(averageLoadKg, other.averageLoadKg, tolerance)
                && close(maxLoadKg, other.maxLoadKg, tolerance)
                && close(totalLoadKg, other.totalLoadKg, tolerance);
    }

    private static boolean allFinite(double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                return false;
            }
        }
        return true;
    }

    private static boolean close(double first, double second, double tolerance) {
        double scale = Math.max(1.0, Math.max(Math.abs(first), Math.abs(second)));
        return Math.abs(first - second) <= tolerance * scale;
    }
}

