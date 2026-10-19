package ru.itmo.reactivejava.elevator.statistics;

import ru.itmo.reactivejava.elevator.domain.AlarmCode;
import ru.itmo.reactivejava.elevator.domain.ElevatorState;
import ru.itmo.reactivejava.elevator.domain.ElevatorTelemetry;

import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;

public final class StatisticsAccumulator {
    private final EnumMap<ElevatorState, DoubleSummaryStatistics> loadByState =
            new EnumMap<>(ElevatorState.class);
    private final EnumMap<AlarmCode, Long> alarmCounts = new EnumMap<>(AlarmCode.class);

    public StatisticsAccumulator() {
        for (ElevatorState state : ElevatorState.values()) {
            loadByState.put(state, new DoubleSummaryStatistics());
        }
        for (AlarmCode alarm : AlarmCode.values()) {
            alarmCounts.put(alarm, 0L);
        }
    }

    public void add(ElevatorTelemetry telemetry) {
        loadByState.get(telemetry.state()).accept(telemetry.loadKg());
        for (AlarmCode alarm : telemetry.activeAlarms()) {
            alarmCounts.merge(alarm, 1L, Long::sum);
        }
    }

    public StatisticsAccumulator combine(StatisticsAccumulator other) {
        for (ElevatorState state : ElevatorState.values()) {
            loadByState.get(state).combine(other.loadByState.get(state));
        }
        for (AlarmCode alarm : AlarmCode.values()) {
            alarmCounts.merge(alarm, other.alarmCounts.get(alarm), Long::sum);
        }
        return this;
    }

    public FleetStatistics finish() {
        return FleetStatistics.from(loadByState, alarmCounts);
    }
}

