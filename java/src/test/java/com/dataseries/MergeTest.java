package com.dataseries;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class MergeTest {

    private static sealed interface Option<T extends Comparable<T>> extends Comparable<Option<T>>
            permits Option.None, Option.Some {
        @Override
        default int compareTo(Option<T> o) {
            switch (this) {
                case None<T> none -> {
                    switch (o) {
                        case None<T> none2 -> {
                            return 0;
                        }
                        case Some<T> v -> {
                            return -1;
                        }
                    }
                }
                case Some<T> v1 -> {
                    switch (o) {
                        case None<T> n -> {
                            return 1;
                        }
                        case Some<T> v2 -> {
                            return v1.value.compareTo(v2.value);
                        }
                    }
                }
            }
        }

        static record None<T extends Comparable<T>>() implements Option<T> {

        }

        static record Some<T extends Comparable<T>>(T value) implements Option<T> {
        }

        static <T extends Comparable<T>> Option<T> some(T value) {
            return new Some<>(value);
        }

        static <T extends Comparable<T>> Option<T> none() {
            return new None<>();
        }
    }

    @Test
    public void MergeWithEmptyValueTest() {
        List<DataPoint<Integer, String>> x = List.of();
        var expected = new Object[] {};
        var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void MergeWithSameValueTest() {
        List<DataPoint<Integer, Option<Integer>>> x = List.of(
                Series.datapoint(1, Option.some(10)),
                Series.datapoint(5, Option.some(10)),
                Series.datapoint(10, Option.none()));

        var expected = List.of(Series.datapoint(1, Option.some(10)), Series.datapoint(10, Option.none())).toArray();

        var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }

    @Test
    public void MergeWithDifferentValueTest() {
        List<DataPoint<Integer, Option<Integer>>> x = List.of(
                Series.datapoint(1, Option.some(10)),
                Series.datapoint(5, Option.some(100)),
                Series.datapoint(10, Option.none()));

        var expected = List.of(
                Series.datapoint(1, Option.some(10)),
                Series.datapoint(5, Option.some(100)),
                Series.datapoint(10, Option.none())).toArray();

        var actual = Series.merge(x).stream().toArray();
        assertArrayEquals(expected, actual);
    }
}
