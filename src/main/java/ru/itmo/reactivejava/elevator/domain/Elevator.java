package ru.itmo.reactivejava.elevator.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class Elevator {
    private final String id;
    private final String buildingId;
    private final String model;
    private final int maxLoadKg;
    private final int maxPassengers;
    private final double ratedSpeedMetersPerSecond;
    private final ElevatorType type;
    private final LocalDate commissionedOn;

    public Elevator(
            String id,
            String buildingId,
            String model,
            int maxLoadKg,
            int maxPassengers,
            double ratedSpeedMetersPerSecond,
            ElevatorType type,
            LocalDate commissionedOn
    ) {
        this.id = requireNonBlank(id, "id");
        this.buildingId = requireNonBlank(buildingId, "buildingId");
        this.model = requireNonBlank(model, "model");
        if (maxLoadKg <= 0) {
            throw new IllegalArgumentException("maxLoadKg must be positive");
        }
        if (maxPassengers <= 0) {
            throw new IllegalArgumentException("maxPassengers must be positive");
        }
        if (!Double.isFinite(ratedSpeedMetersPerSecond)
                || ratedSpeedMetersPerSecond <= 0.0) {
            throw new IllegalArgumentException(
                    "ratedSpeedMetersPerSecond must be finite and positive"
            );
        }
        this.maxLoadKg = maxLoadKg;
        this.maxPassengers = maxPassengers;
        this.ratedSpeedMetersPerSecond = ratedSpeedMetersPerSecond;
        this.type = Objects.requireNonNull(type, "type");
        this.commissionedOn = Objects.requireNonNull(commissionedOn, "commissionedOn");
    }

    public String id() {
        return id;
    }

    public String buildingId() {
        return buildingId;
    }

    public String model() {
        return model;
    }

    public int maxLoadKg() {
        return maxLoadKg;
    }

    public int maxPassengers() {
        return maxPassengers;
    }

    public double ratedSpeedMetersPerSecond() {
        return ratedSpeedMetersPerSecond;
    }

    public ElevatorType type() {
        return type;
    }

    public LocalDate commissionedOn() {
        return commissionedOn;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Elevator elevator && id.equals(elevator.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Elevator{" +
                "id='" + id + '\'' +
                ", buildingId='" + buildingId + '\'' +
                ", model='" + model + '\'' +
                ", maxLoadKg=" + maxLoadKg +
                ", type=" + type +
                '}';
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName);
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}

