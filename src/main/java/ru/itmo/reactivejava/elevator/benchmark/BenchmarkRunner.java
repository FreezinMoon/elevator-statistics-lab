package ru.itmo.reactivejava.elevator.benchmark;

import ru.itmo.reactivejava.elevator.domain.AlarmCode;
import ru.itmo.reactivejava.elevator.domain.Building;
import ru.itmo.reactivejava.elevator.domain.Elevator;
import ru.itmo.reactivejava.elevator.domain.ElevatorState;
import ru.itmo.reactivejava.elevator.domain.ElevatorTelemetry;
import ru.itmo.reactivejava.elevator.generator.BuildingGenerator;
import ru.itmo.reactivejava.elevator.generator.ElevatorGenerator;
import ru.itmo.reactivejava.elevator.generator.ElevatorTelemetryGenerator;
import ru.itmo.reactivejava.elevator.statistics.FleetStatistics;
import ru.itmo.reactivejava.elevator.statistics.LoadStatistics;
import ru.itmo.reactivejava.elevator.statistics.StatisticsCalculator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BenchmarkRunner {
    private static final int BUILDING_COUNT = 12;
    private static final int ELEVATOR_COUNT = 60;
    private static final int WARMUP_ROUNDS = 3;
    private static final double COMPARISON_TOLERANCE = 1.0e-12;
    private static volatile long resultSink;

    private BenchmarkRunner() {
    }

    public static void run(List<Integer> sizes, boolean includeParallel) {
        List<Building> buildings = new BuildingGenerator(10_001L)
                .generate(BUILDING_COUNT);
        List<Elevator> elevators = new ElevatorGenerator(20_002L)
                .generate(ELEVATOR_COUNT, buildings);

        System.out.printf(Locale.ROOT,
                "Компания: %d зданий, %d лифтов, доступно процессоров: %d%n",
                buildings.size(),
                elevators.size(),
                Runtime.getRuntime().availableProcessors());
        System.out.printf("Прогрев перед каждым замером: %d раунда(ов).%n%n", WARMUP_ROUNDS);

        FleetStatistics latestStatistics = null;
        for (int size : sizes) {
            List<ElevatorTelemetry> events = new ElevatorTelemetryGenerator(30_003L + size)
                    .generate(size, elevators, buildings);

            verifyUniqueIds(events);
            warmUp(events, includeParallel);

            List<BenchmarkMeasurement> measurements = new ArrayList<>();
            measurements.add(measure(CalculationMethod.ITERATIVE_LOOP, events));
            measurements.add(measure(CalculationMethod.STANDARD_STREAM, events));
            measurements.add(measure(CalculationMethod.CUSTOM_COLLECTOR, events));
            if (includeParallel) {
                measurements.add(measure(
                        CalculationMethod.PARALLEL_CUSTOM_COLLECTOR,
                        events
                ));
            }

            verifyEqualResults(measurements);
            printMeasurements(size, measurements);
            latestStatistics = measurements.get(0).result();
        }

        if (latestStatistics != null) {
            printStatistics(latestStatistics);
        }
    }

    private static void warmUp(List<ElevatorTelemetry> events, boolean includeParallel) {
        for (int round = 0; round < WARMUP_ROUNDS; round++) {
            consume(StatisticsCalculator.calculateIteratively(events));
            consume(StatisticsCalculator.calculateWithStandardCollectors(events));
            consume(StatisticsCalculator.calculateWithCustomCollector(events));
            if (includeParallel) {
                consume(StatisticsCalculator.calculateWithParallelCustomCollector(events));
            }
        }
    }

    private static BenchmarkMeasurement measure(
            CalculationMethod method,
            List<ElevatorTelemetry> events
    ) {
        long startedNanos = System.nanoTime();
        FleetStatistics result = switch (method) {
            case ITERATIVE_LOOP -> StatisticsCalculator.calculateIteratively(events);
            case STANDARD_STREAM ->
                    StatisticsCalculator.calculateWithStandardCollectors(events);
            case CUSTOM_COLLECTOR ->
                    StatisticsCalculator.calculateWithCustomCollector(events);
            case PARALLEL_CUSTOM_COLLECTOR ->
                    StatisticsCalculator.calculateWithParallelCustomCollector(events);
        };
        long finishedNanos = System.nanoTime();

        consume(result);
        return new BenchmarkMeasurement(
                method,
                events.size(),
                startedNanos,
                finishedNanos,
                result
        );
    }

    private static void verifyUniqueIds(List<ElevatorTelemetry> events) {
        long distinctIds = events.stream()
                .mapToLong(ElevatorTelemetry::eventId)
                .distinct()
                .count();
        if (distinctIds != events.size()) {
            throw new IllegalStateException("generator produced duplicate event identifiers");
        }
    }

    private static void verifyEqualResults(List<BenchmarkMeasurement> measurements) {
        FleetStatistics expected = measurements.get(0).result();
        for (int index = 1; index < measurements.size(); index++) {
            BenchmarkMeasurement measurement = measurements.get(index);
            if (!expected.approximatelyEquals(
                    measurement.result(),
                    COMPARISON_TOLERANCE
            )) {
                throw new IllegalStateException(
                        "calculation result differs for " + measurement.method()
                );
            }
        }
    }

    private static void printMeasurements(
            int size,
            List<BenchmarkMeasurement> measurements
    ) {
        System.out.printf("Размер коллекции: %,d%n", size);
        System.out.printf(
                "%-38s %20s %20s %14s%n",
                "Метод",
                "Начало, нс",
                "Окончание, нс",
                "Время, мс"
        );
        for (BenchmarkMeasurement measurement : measurements) {
            System.out.printf(
                    Locale.ROOT,
                    "%-38s %20d %20d %14.3f%n",
                    measurement.method().displayName(),
                    measurement.startedNanos(),
                    measurement.finishedNanos(),
                    measurement.durationMillis()
            );
        }
        System.out.println("Результаты всех методов совпали.\n");
    }

    private static void printStatistics(FleetStatistics statistics) {
        System.out.println("Статистика для последней коллекции:");
        System.out.printf("Всего событий: %,d%n", statistics.totalEvents());
        System.out.printf(
                "Всего срабатываний аварий: %,d%n",
                statistics.totalAlarmOccurrences()
        );

        System.out.printf(
                "%-16s %12s %14s %14s %14s%n",
                "Состояние",
                "События",
                "Мин. нагрузка",
                "Средняя",
                "Макс. нагрузка"
        );
        for (ElevatorState state : ElevatorState.values()) {
            LoadStatistics load = statistics.loadByState().get(state);
            System.out.printf(
                    Locale.ROOT,
                    "%-16s %,12d %,14.2f %,14.2f %,14.2f%n",
                    state,
                    load.eventCount(),
                    load.minLoadKg(),
                    load.averageLoadKg(),
                    load.maxLoadKg()
            );
        }

        System.out.println("Срабатывания по типам:");
        for (AlarmCode alarm : AlarmCode.values()) {
            System.out.printf(
                    "  %-20s %,d%n",
                    alarm,
                    statistics.alarmCounts().get(alarm)
            );
        }
    }

    private static void consume(FleetStatistics statistics) {
        resultSink = statistics.totalEvents() * 31L
                + statistics.totalAlarmOccurrences();
    }
}

