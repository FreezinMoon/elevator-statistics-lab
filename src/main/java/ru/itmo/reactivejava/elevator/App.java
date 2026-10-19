package ru.itmo.reactivejava.elevator;

import ru.itmo.reactivejava.elevator.benchmark.BenchmarkRunner;

import java.util.List;

public class App {
    public static void main(String[] args) {
        BenchmarkRunner.run(List.of(5_000, 50_000, 250_000));
    }
}
