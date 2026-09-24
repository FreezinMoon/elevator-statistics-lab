package ru.itmo.reactivejava.elevator.domain;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class ElevatorTelemetry {
    private final long eventId;
    private final String sensorId;
    private final LocalDateTime capturedAt;
    private final ElevatorState state;
    private final FloorPosition position;
    private final Set<AlarmCode> activeAlarms;
    private final int passengerCount;
    private final double loadKg;
    private final double speedMetersPerSecond;
    private final Elevator elevator;
    private final Building building;

    public ElevatorTelemetry(
            long eventId,
            String sensorId,
            LocalDateTime capturedAt,
            ElevatorState state,
            FloorPosition position,
            Set<AlarmCode> activeAlarms,
            int passengerCount,
            double loadKg,
            double speedMetersPerSecond,
            Elevator elevator,
            Building building
    ) {
        if (eventId < 0) {
            throw new IllegalArgumentException("eventId must not be negative");
        }
        this.eventId = eventId;
        this.sensorId = requireNonBlank(sensorId, "sensorId");
        this.capturedAt = Objects.requireNonNull(capturedAt, "capturedAt");
        this.state = Objects.requireNonNull(state, "state");
        this.position = Objects.requireNonNull(position, "position");
        this.elevator = Objects.requireNonNull(elevator, "elevator");
        this.building = Objects.requireNonNull(building, "building");

        if (!elevator.buildingId().equals(building.id())) {
            throw new IllegalArgumentException("elevator and building do not match");
        }
        if (!building.containsFloor(position.floor())) {
            throw new IllegalArgumentException("position is outside the building");
        }
        if (passengerCount < 0 || passengerCount > elevator.maxPassengers()) {
            throw new IllegalArgumentException("passengerCount exceeds elevator limits");
        }
        if (!Double.isFinite(loadKg) || loadKg < 0.0) {
            throw new IllegalArgumentException("loadKg must be finite and non-negative");
        }
        if (!Double.isFinite(speedMetersPerSecond) || speedMetersPerSecond < 0.0) {
            throw new IllegalArgumentException(
                    "speedMetersPerSecond must be finite and non-negative"
            );
        }
        if (speedMetersPerSecond > elevator.ratedSpeedMetersPerSecond()) {
            throw new IllegalArgumentException("speed exceeds the elevator rated speed");
        }

        boolean moving = state == ElevatorState.MOVING_UP
                || state == ElevatorState.MOVING_DOWN;
        if (moving && speedMetersPerSecond == 0.0) {
            throw new IllegalArgumentException("moving elevator must have positive speed");
        }
        if (!moving && speedMetersPerSecond != 0.0) {
            throw new IllegalArgumentException("stationary elevator must have zero speed");
        }
        if (!moving && position.progressToNextFloor() != 0.0) {
            throw new IllegalArgumentException(
                    "stationary elevator must be positioned exactly at a floor"
            );
        }

        this.activeAlarms = immutableAlarmSet(activeAlarms);
        boolean overloaded = loadKg > elevator.maxLoadKg();
        if (overloaded != this.activeAlarms.contains(AlarmCode.OVERLOAD)) {
            throw new IllegalArgumentException(
                    "OVERLOAD alarm must exactly match the measured load"
            );
        }
        if (this.activeAlarms.contains(AlarmCode.EMERGENCY_STOP)
                && state != ElevatorState.EMERGENCY) {
            throw new IllegalArgumentException(
                    "EMERGENCY_STOP is only valid in EMERGENCY state"
            );
        }

        this.passengerCount = passengerCount;
        this.loadKg = loadKg;
        this.speedMetersPerSecond = speedMetersPerSecond;
    }

    public long eventId() {
        return eventId;
    }

    public String sensorId() {
        return sensorId;
    }

    public LocalDateTime capturedAt() {
        return capturedAt;
    }

    public ElevatorState state() {
        return state;
    }

    public FloorPosition position() {
        return position;
    }

    public Set<AlarmCode> activeAlarms() {
        return activeAlarms;
    }

    public int passengerCount() {
        return passengerCount;
    }

    public double loadKg() {
        return loadKg;
    }

    public double speedMetersPerSecond() {
        return speedMetersPerSecond;
    }

    public Elevator elevator() {
        return elevator;
    }

    public Building building() {
        return building;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof ElevatorTelemetry telemetry
                && eventId == telemetry.eventId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(eventId);
    }

    @Override
    public String toString() {
        return "ElevatorTelemetry{" +
                "eventId=" + eventId +
                ", sensorId='" + sensorId + '\'' +
                ", capturedAt=" + capturedAt +
                ", state=" + state +
                ", position=" + position +
                ", activeAlarms=" + activeAlarms +
                ", passengerCount=" + passengerCount +
                ", loadKg=" + loadKg +
                ", speedMetersPerSecond=" + speedMetersPerSecond +
                ", elevator=" + elevator.id() +
                ", building=" + building.id() +
                '}';
    }

    private static Set<AlarmCode> immutableAlarmSet(Set<AlarmCode> alarms) {
        Objects.requireNonNull(alarms, "activeAlarms");
        if (alarms.isEmpty()) {
            return Set.of();
        }
        return Collections.unmodifiableSet(EnumSet.copyOf(alarms));
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}

