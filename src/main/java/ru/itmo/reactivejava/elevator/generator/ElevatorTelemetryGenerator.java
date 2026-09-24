package ru.itmo.reactivejava.elevator.generator;

import ru.itmo.reactivejava.elevator.domain.AlarmCode;
import ru.itmo.reactivejava.elevator.domain.Building;
import ru.itmo.reactivejava.elevator.domain.Elevator;
import ru.itmo.reactivejava.elevator.domain.ElevatorState;
import ru.itmo.reactivejava.elevator.domain.ElevatorTelemetry;
import ru.itmo.reactivejava.elevator.domain.FloorPosition;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;

public final class ElevatorTelemetryGenerator {
    private static final LocalDateTime FIRST_EVENT_TIME =
            LocalDateTime.of(2026, 1, 1, 8, 0);
    private static final long EVENT_INTERVAL_NANOS = 20_000_000L;

    private final SplittableRandom random;

    public ElevatorTelemetryGenerator(long seed) {
        this.random = new SplittableRandom(seed);
    }

    public List<ElevatorTelemetry> generate(
            int count,
            List<Elevator> elevators,
            List<Building> buildings
    ) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        Objects.requireNonNull(elevators, "elevators");
        Objects.requireNonNull(buildings, "buildings");
        if (count > 0 && elevators.isEmpty()) {
            throw new IllegalArgumentException("at least one elevator is required");
        }

        Map<String, Building> buildingsById = indexBuildings(buildings);
        List<ElevatorTelemetry> events = new ArrayList<>(count);

        for (int index = 0; index < count; index++) {
            long eventId = index + 1L;
            Elevator elevator = elevators.get(random.nextInt(elevators.size()));
            Building building = buildingsById.get(elevator.buildingId());
            if (building == null) {
                throw new IllegalArgumentException(
                        "building not found for elevator " + elevator.id()
                );
            }

            ElevatorState state = randomState();
            FloorPosition position = randomPosition(state, building);
            int passengerCount = randomPassengerCount(state, elevator);
            boolean overload = state != ElevatorState.MAINTENANCE
                    && random.nextDouble() < 0.015;
            double loadKg = randomLoad(passengerCount, elevator, overload);
            double speed = randomSpeed(state, elevator);
            EnumSet<AlarmCode> alarms = randomAlarms(state, overload);

            events.add(new ElevatorTelemetry(
                    eventId,
                    "SNS-" + elevator.id(),
                    FIRST_EVENT_TIME.plusNanos(eventId * EVENT_INTERVAL_NANOS),
                    state,
                    position,
                    alarms,
                    passengerCount,
                    loadKg,
                    speed,
                    elevator,
                    building
            ));
        }

        return List.copyOf(events);
    }

    private Map<String, Building> indexBuildings(List<Building> buildings) {
        Map<String, Building> result = new HashMap<>();
        for (Building building : buildings) {
            Building previous = result.put(building.id(), building);
            if (previous != null) {
                throw new IllegalArgumentException("duplicate building id: " + building.id());
            }
        }
        return result;
    }

    private ElevatorState randomState() {
        int value = random.nextInt(100);
        if (value < 18) {
            return ElevatorState.IDLE;
        }
        if (value < 43) {
            return ElevatorState.MOVING_UP;
        }
        if (value < 68) {
            return ElevatorState.MOVING_DOWN;
        }
        if (value < 90) {
            return ElevatorState.DOORS_OPEN;
        }
        if (value < 97) {
            return ElevatorState.MAINTENANCE;
        }
        return ElevatorState.EMERGENCY;
    }

    private FloorPosition randomPosition(ElevatorState state, Building building) {
        int lowestFloor = building.lowestFloor();
        int highestFloor = building.highestFloor();

        return switch (state) {
            case MOVING_UP -> new FloorPosition(
                    random.nextInt(lowestFloor, highestFloor),
                    random.nextDouble(0.01, 0.99)
            );
            case MOVING_DOWN -> new FloorPosition(
                    random.nextInt(lowestFloor + 1, highestFloor + 1),
                    random.nextDouble(0.01, 0.99)
            );
            default -> new FloorPosition(
                    random.nextInt(lowestFloor, highestFloor + 1),
                    0.0
            );
        };
    }

    private int randomPassengerCount(ElevatorState state, Elevator elevator) {
        if (state == ElevatorState.MAINTENANCE) {
            return 0;
        }
        return random.nextInt(elevator.maxPassengers() + 1);
    }

    private double randomLoad(int passengerCount, Elevator elevator, boolean overload) {
        if (overload) {
            return elevator.maxLoadKg() * random.nextDouble(1.01, 1.13);
        }
        if (passengerCount == 0) {
            return 0.0;
        }
        double estimatedLoad = passengerCount * random.nextDouble(55.0, 95.0);
        return Math.min(estimatedLoad, elevator.maxLoadKg() * 0.98);
    }

    private double randomSpeed(ElevatorState state, Elevator elevator) {
        if (state != ElevatorState.MOVING_UP && state != ElevatorState.MOVING_DOWN) {
            return 0.0;
        }
        return random.nextDouble(0.1, elevator.ratedSpeedMetersPerSecond());
    }

    private EnumSet<AlarmCode> randomAlarms(ElevatorState state, boolean overload) {
        EnumSet<AlarmCode> alarms = EnumSet.noneOf(AlarmCode.class);

        if (overload) {
            alarms.add(AlarmCode.OVERLOAD);
        }
        if (state == ElevatorState.DOORS_OPEN && random.nextDouble() < 0.02) {
            alarms.add(AlarmCode.DOOR_BLOCKED);
        }
        if ((state == ElevatorState.MOVING_UP
                || state == ElevatorState.MOVING_DOWN
                || state == ElevatorState.MAINTENANCE)
                && random.nextDouble() < 0.006) {
            alarms.add(AlarmCode.MOTOR_OVERHEAT);
        }
        if (random.nextDouble() < 0.002) {
            alarms.add(AlarmCode.SENSOR_FAILURE);
        }
        if (state == ElevatorState.EMERGENCY) {
            alarms.add(AlarmCode.EMERGENCY_STOP);
        }

        return alarms;
    }
}

