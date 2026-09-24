package ru.itmo.reactivejava.elevator.statistics;

import ru.itmo.reactivejava.elevator.domain.AlarmCode;
import ru.itmo.reactivejava.elevator.domain.ElevatorState;

import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public record FleetStatistics(
        long totalEvents,
        long totalAlarmOccurrences,
        Map<ElevatorState, LoadStatistics> loadByState,
        Map<AlarmCode, Long> alarmCounts
) {
    public FleetStatistics {
        if (totalEvents < 0 || totalAlarmOccurrences < 0) {
            throw new IllegalArgumentException("total values must not be negative");
        }
        Objects.requireNonNull(loadByState, "loadByState");
        Objects.requireNonNull(alarmCounts, "alarmCounts");

        EnumMap<ElevatorState, LoadStatistics> loadCopy =
                new EnumMap<>(ElevatorState.class);
        for (ElevatorState state : ElevatorState.values()) {
            loadCopy.put(state, Objects.requireNonNull(
                    loadByState.getOrDefault(state, LoadStatistics.empty()),
                    "load statistics for " + state
            ));
        }

        EnumMap<AlarmCode, Long> alarmCopy = new EnumMap<>(AlarmCode.class);
        for (AlarmCode alarm : AlarmCode.values()) {
            long count = alarmCounts.getOrDefault(alarm, 0L);
            if (count < 0) {
                throw new IllegalArgumentException("alarm count must not be negative");
            }
            alarmCopy.put(alarm, count);
        }

        long calculatedEvents = loadCopy.values().stream()
                .mapToLong(LoadStatistics::eventCount)
                .sum();
        long calculatedAlarms = alarmCopy.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        if (calculatedEvents != totalEvents) {
            throw new IllegalArgumentException("totalEvents does not match state statistics");
        }
        if (calculatedAlarms != totalAlarmOccurrences) {
            throw new IllegalArgumentException("totalAlarmOccurrences does not match alarm map");
        }

        loadByState = Collections.unmodifiableMap(loadCopy);
        alarmCounts = Collections.unmodifiableMap(alarmCopy);
    }

    public static FleetStatistics from(
            Map<ElevatorState, DoubleSummaryStatistics> loadSummaries,
            Map<AlarmCode, Long> rawAlarmCounts
    ) {
        Objects.requireNonNull(loadSummaries, "loadSummaries");
        Objects.requireNonNull(rawAlarmCounts, "rawAlarmCounts");

        EnumMap<ElevatorState, LoadStatistics> loads =
                new EnumMap<>(ElevatorState.class);
        for (ElevatorState state : ElevatorState.values()) {
            DoubleSummaryStatistics summary = loadSummaries.get(state);
            loads.put(state, summary == null
                    ? LoadStatistics.empty()
                    : LoadStatistics.from(summary));
        }

        EnumMap<AlarmCode, Long> alarms = new EnumMap<>(AlarmCode.class);
        for (AlarmCode alarm : AlarmCode.values()) {
            alarms.put(alarm, rawAlarmCounts.getOrDefault(alarm, 0L));
        }

        long totalEvents = loads.values().stream()
                .mapToLong(LoadStatistics::eventCount)
                .sum();
        long totalAlarms = alarms.values().stream()
                .mapToLong(Long::longValue)
                .sum();
        return new FleetStatistics(totalEvents, totalAlarms, loads, alarms);
    }

    public boolean approximatelyEquals(FleetStatistics other, double tolerance) {
        if (other == null
                || totalEvents != other.totalEvents
                || totalAlarmOccurrences != other.totalAlarmOccurrences
                || !alarmCounts.equals(other.alarmCounts)) {
            return false;
        }
        for (ElevatorState state : ElevatorState.values()) {
            if (!loadByState.get(state)
                    .approximatelyEquals(other.loadByState.get(state), tolerance)) {
                return false;
            }
        }
        return true;
    }
}

