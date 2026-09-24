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

import java.util.List;

public class BenchmarkRunner {
    private static final int BUILDING_COUNT = 12;
    private static final int ELEVATOR_COUNT = 60;

    public static void run(List<Integer> sizes) {
        List<Building> buildings = new BuildingGenerator(10_001L)
                .generate(BUILDING_COUNT);
        List<Elevator> elevators = new ElevatorGenerator(20_002L)
                .generate(ELEVATOR_COUNT, buildings);

        System.out.println("Зданий: " + buildings.size());
        System.out.println("Лифтов: " + elevators.size());

        FleetStatistics latestStatistics = null;
        for (int size : sizes) {
            List<ElevatorTelemetry> events = new ElevatorTelemetryGenerator(30_003L + size)
                    .generate(size, elevators, buildings);

            long loopStart = System.nanoTime();
            FleetStatistics loopResult = StatisticsCalculator.calculateIteratively(events);
            long loopEnd = System.nanoTime();

            long streamStart = System.nanoTime();
            FleetStatistics streamResult =
                    StatisticsCalculator.calculateWithStandardCollectors(events);
            long streamEnd = System.nanoTime();

            long collectorStart = System.nanoTime();
            FleetStatistics collectorResult =
                    StatisticsCalculator.calculateWithCustomCollector(events);
            long collectorEnd = System.nanoTime();

            boolean resultsMatch = loopResult.approximatelyEquals(streamResult, 1.0e-12)
                    && loopResult.approximatelyEquals(collectorResult, 1.0e-12);

            System.out.println("\nРазмер коллекции: " + size);
            printTime("Цикл", loopStart, loopEnd);
            printTime("Стандартные коллекторы", streamStart, streamEnd);
            printTime("Собственный коллектор", collectorStart, collectorEnd);
            System.out.println("Результаты совпадают: " + resultsMatch);

            if (!resultsMatch) {
                throw new IllegalStateException("Результаты расчётов отличаются");
            }

            latestStatistics = loopResult;
        }

        printStatistics(latestStatistics);
    }

    private static void printTime(String method, long start, long end) {
        double milliseconds = (end - start) / 1_000_000.0;
        System.out.printf(
                "%s: начало = %d нс, конец = %d нс, время = %.3f мс%n",
                method,
                start,
                end,
                milliseconds
        );
    }

    private static void printStatistics(FleetStatistics statistics) {
        System.out.println("\nИтоговая статистика:");
        System.out.println("Всего событий: " + statistics.totalEvents());
        System.out.println("Всего аварий: " + statistics.totalAlarmOccurrences());

        for (ElevatorState state : ElevatorState.values()) {
            LoadStatistics load = statistics.loadByState().get(state);
            System.out.printf(
                    "%s: событий = %d, нагрузка min/avg/max = %.2f/%.2f/%.2f кг%n",
                    state,
                    load.eventCount(),
                    load.minLoadKg(),
                    load.averageLoadKg(),
                    load.maxLoadKg()
            );
        }

        System.out.println("Аварии по типам:");
        for (AlarmCode alarm : AlarmCode.values()) {
            System.out.println(alarm + ": " + statistics.alarmCounts().get(alarm));
        }
    }
}
