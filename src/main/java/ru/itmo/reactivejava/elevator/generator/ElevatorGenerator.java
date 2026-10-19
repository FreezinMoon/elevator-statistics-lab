package ru.itmo.reactivejava.elevator.generator;

import ru.itmo.reactivejava.elevator.domain.Building;
import ru.itmo.reactivejava.elevator.domain.Elevator;
import ru.itmo.reactivejava.elevator.domain.ElevatorType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SplittableRandom;

public final class ElevatorGenerator {
    private static final int[] LOAD_OPTIONS_KG = {450, 630, 800, 1000, 1250, 1600};
    private static final double[] SPEED_OPTIONS_MPS = {0.63, 1.0, 1.6, 2.0, 2.5, 3.0};
    private static final LocalDate LATEST_COMMISSIONING = LocalDate.of(2025, 12, 31);

    private final SplittableRandom random;

    public ElevatorGenerator(long seed) {
        this.random = new SplittableRandom(seed);
    }

    public List<Elevator> generate(int count, List<Building> buildings) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }
        Objects.requireNonNull(buildings, "buildings");
        if (count > 0 && buildings.isEmpty()) {
            throw new IllegalArgumentException("at least one building is required");
        }

        List<Elevator> elevators = new ArrayList<>(count);
        ElevatorType[] types = ElevatorType.values();

        for (int index = 0; index < count; index++) {
            Building building = buildings.get(random.nextInt(buildings.size()));
            int maxLoadKg = LOAD_OPTIONS_KG[random.nextInt(LOAD_OPTIONS_KG.length)];
            int maxPassengers = Math.max(4, maxLoadKg / 75);
            double ratedSpeed = SPEED_OPTIONS_MPS[random.nextInt(SPEED_OPTIONS_MPS.length)];
            LocalDate earliestCommissioning = building.openedOn().isAfter(LocalDate.of(1985, 1, 1))
                    ? building.openedOn()
                    : LocalDate.of(1985, 1, 1);

            elevators.add(new Elevator(
                    "ELV-%05d".formatted(index + 1),
                    building.id(),
                    "Model-%c%d".formatted((char) ('A' + random.nextInt(6)), random.nextInt(100, 999)),
                    maxLoadKg,
                    maxPassengers,
                    ratedSpeed,
                    types[random.nextInt(types.length)],
                    randomDate(earliestCommissioning, LATEST_COMMISSIONING)
            ));
        }

        return List.copyOf(elevators);
    }

    private LocalDate randomDate(LocalDate fromInclusive, LocalDate toInclusive) {
        long day = random.nextLong(fromInclusive.toEpochDay(), toInclusive.toEpochDay() + 1);
        return LocalDate.ofEpochDay(day);
    }
}

