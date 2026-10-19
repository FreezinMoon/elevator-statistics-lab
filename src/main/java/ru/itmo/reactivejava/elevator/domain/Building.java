package ru.itmo.reactivejava.elevator.domain;

import java.time.LocalDate;
import java.util.Objects;

public final class Building {
    private final String id;
    private final String name;
    private final String address;
    private final int highestFloor;
    private final int basementFloors;
    private final BuildingType type;
    private final LocalDate openedOn;

    public Building(
            String id,
            String name,
            String address,
            int highestFloor,
            int basementFloors,
            BuildingType type,
            LocalDate openedOn
    ) {
        this.id = requireNonBlank(id, "id");
        this.name = requireNonBlank(name, "name");
        this.address = requireNonBlank(address, "address");
        if (highestFloor < 1) {
            throw new IllegalArgumentException("highestFloor must be positive");
        }
        if (basementFloors < 0) {
            throw new IllegalArgumentException("basementFloors must not be negative");
        }
        this.highestFloor = highestFloor;
        this.basementFloors = basementFloors;
        this.type = Objects.requireNonNull(type, "type");
        this.openedOn = Objects.requireNonNull(openedOn, "openedOn");
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String address() {
        return address;
    }

    public int highestFloor() {
        return highestFloor;
    }

    public int basementFloors() {
        return basementFloors;
    }

    public int lowestFloor() {
        return -basementFloors;
    }

    public BuildingType type() {
        return type;
    }

    public LocalDate openedOn() {
        return openedOn;
    }

    public boolean containsFloor(int floor) {
        return floor >= lowestFloor() && floor <= highestFloor;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof Building building && id.equals(building.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Building{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", floors=" + lowestFloor() + ".." + highestFloor +
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

