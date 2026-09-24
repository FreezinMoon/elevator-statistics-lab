package ru.itmo.reactivejava.elevator.statistics;

import ru.itmo.reactivejava.elevator.domain.AlarmCode;
import ru.itmo.reactivejava.elevator.domain.ElevatorState;
import ru.itmo.reactivejava.elevator.domain.ElevatorTelemetry;

import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class StatisticsCalculator {
    private static final ElevatorStatisticsCollector CUSTOM_COLLECTOR =
            new ElevatorStatisticsCollector();

    private StatisticsCalculator() {
    }

    public static FleetStatistics calculateIteratively(List<ElevatorTelemetry> events) {
        Objects.requireNonNull(events, "events");

        EnumMap<ElevatorState, DoubleSummaryStatistics> loadByState =
                new EnumMap<>(ElevatorState.class);
        EnumMap<AlarmCode, Long> alarmCounts = new EnumMap<>(AlarmCode.class);
        for (ElevatorState state : ElevatorState.values()) {
            loadByState.put(state, new DoubleSummaryStatistics());
        }
        for (AlarmCode alarm : AlarmCode.values()) {
            alarmCounts.put(alarm, 0L);
        }

        for (ElevatorTelemetry event : events) {
            loadByState.get(event.state()).accept(event.loadKg());
            for (AlarmCode alarm : event.activeAlarms()) {
                alarmCounts.merge(alarm, 1L, Long::sum);
            }
        }

        return FleetStatistics.from(loadByState, alarmCounts);
    }

    public static FleetStatistics calculateWithStandardCollectors(
            List<ElevatorTelemetry> events
    ) {
        Objects.requireNonNull(events, "events");

        Collector<ElevatorTelemetry, ?, EnumMap<ElevatorState, DoubleSummaryStatistics>>
                loadCollector = Collectors.groupingBy(
                        ElevatorTelemetry::state,
                        () -> new EnumMap<>(ElevatorState.class),
                        Collectors.summarizingDouble(ElevatorTelemetry::loadKg)
                );

        Collector<AlarmCode, ?, EnumMap<AlarmCode, Long>> alarmGroupingCollector =
                Collectors.groupingBy(
                        Function.identity(),
                        () -> new EnumMap<>(AlarmCode.class),
                        Collectors.counting()
                );

        Collector<ElevatorTelemetry, ?, EnumMap<AlarmCode, Long>> alarmCollector =
                Collectors.flatMapping(
                        event -> event.activeAlarms().stream(),
                        alarmGroupingCollector
                );

        return events.stream().collect(Collectors.teeing(
                loadCollector,
                alarmCollector,
                FleetStatistics::from
        ));
    }

    public static FleetStatistics calculateWithCustomCollector(
            List<ElevatorTelemetry> events
    ) {
        Objects.requireNonNull(events, "events");
        return events.stream().collect(CUSTOM_COLLECTOR);
    }

    public static FleetStatistics calculateWithParallelCustomCollector(
            List<ElevatorTelemetry> events
    ) {
        Objects.requireNonNull(events, "events");
        return events.parallelStream().collect(CUSTOM_COLLECTOR);
    }
}

