package ru.itmo.reactivejava.elevator.domain;

/**
 * @param floor current or last passed floor
 * @param progressToNextFloor relative position between floors in the range [0, 1)
 */
public record FloorPosition(int floor, double progressToNextFloor) {

    public FloorPosition {
        if (!Double.isFinite(progressToNextFloor)
                || progressToNextFloor < 0.0
                || progressToNextFloor >= 1.0) {
            throw new IllegalArgumentException(
                    "progressToNextFloor must be finite and belong to [0, 1)"
            );
        }
    }
}

