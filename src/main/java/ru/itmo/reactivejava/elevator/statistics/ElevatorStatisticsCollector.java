package ru.itmo.reactivejava.elevator.statistics;

import ru.itmo.reactivejava.elevator.domain.ElevatorTelemetry;

import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public final class ElevatorStatisticsCollector implements Collector<
        ElevatorTelemetry,
        StatisticsAccumulator,
        FleetStatistics
        > {

    @Override
    public Supplier<StatisticsAccumulator> supplier() {
        return StatisticsAccumulator::new;
    }

    @Override
    public BiConsumer<StatisticsAccumulator, ElevatorTelemetry> accumulator() {
        return StatisticsAccumulator::add;
    }

    @Override
    public BinaryOperator<StatisticsAccumulator> combiner() {
        return StatisticsAccumulator::combine;
    }

    @Override
    public Function<StatisticsAccumulator, FleetStatistics> finisher() {
        return StatisticsAccumulator::finish;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.UNORDERED);
    }
}

