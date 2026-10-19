package ru.itmo.reactivejava.elevator.generator;

import ru.itmo.reactivejava.elevator.domain.Building;
import ru.itmo.reactivejava.elevator.domain.BuildingType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

public final class BuildingGenerator {
    private static final String[] STREET_NAMES = {
            "Central Avenue",
            "Riverside Street",
            "Technology Prospect",
            "Northern Boulevard",
            "Harbour Road",
            "University Embankment"
    };
    private static final LocalDate EARLIEST_OPENING = LocalDate.of(1960, 1, 1);
    private static final LocalDate LATEST_OPENING = LocalDate.of(2020, 12, 31);

    private final SplittableRandom random;

    public BuildingGenerator(long seed) {
        this.random = new SplittableRandom(seed);
    }

    public List<Building> generate(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must not be negative");
        }

        List<Building> buildings = new ArrayList<>(count);
        BuildingType[] types = BuildingType.values();

        for (int index = 0; index < count; index++) {
            String id = "BLD-%04d".formatted(index + 1);
            BuildingType type = types[random.nextInt(types.length)];
            int highestFloor = random.nextInt(5, 51);
            int basementFloors = random.nextInt(0, 4);
            String street = STREET_NAMES[random.nextInt(STREET_NAMES.length)];
            String address = random.nextInt(1, 250) + " " + street;

            buildings.add(new Building(
                    id,
                    "Company Building %d".formatted(index + 1),
                    address,
                    highestFloor,
                    basementFloors,
                    type,
                    randomDate(EARLIEST_OPENING, LATEST_OPENING)
            ));
        }

        return List.copyOf(buildings);
    }

    private LocalDate randomDate(LocalDate fromInclusive, LocalDate toInclusive) {
        long day = random.nextLong(fromInclusive.toEpochDay(), toInclusive.toEpochDay() + 1);
        return LocalDate.ofEpochDay(day);
    }
}

