package ru.itmo.reactivejava.elevator.benchmark;

public enum CalculationMethod {
    ITERATIVE_LOOP("Итерационный цикл"),
    STANDARD_STREAM("Stream + стандартные коллекторы"),
    CUSTOM_COLLECTOR("Stream + собственный коллектор"),
    PARALLEL_CUSTOM_COLLECTOR("Parallel Stream + свой коллектор");

    private final String displayName;

    CalculationMethod(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

