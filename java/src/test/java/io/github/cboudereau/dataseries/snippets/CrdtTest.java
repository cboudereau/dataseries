package io.github.cboudereau.dataseries.snippets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.DataPoint;
import io.github.cboudereau.dataseries.Series;
import io.github.cboudereau.dataseries.UnionResult;

public class CrdtTest {
    @Test
    public void resolveConflictsTest() {
        final var actual = Series.union(List.of(
                datapoint(1, date(2023, 1, 3), 50),
                end(date(2023, 1, 10))),
                List.of(
                        datapoint(2, date(2023, 1, 4), 100),
                        end(date(2023, 1, 5)),
                        datapoint(2, date(2023, 1, 7), 110),
                        end(date(2023, 1, 9))),
                CrdtTest::resolveConflicts);

        final var expected = List.of(
                datapoint(1, date(2023, 1, 3), 50),
                datapoint(2, date(2023, 1, 4), 100),
                datapoint(1, date(2023, 1, 5), 50),
                datapoint(2, date(2023, 1, 7), 110),
                datapoint(1, date(2023, 1, 9), 50),
                end(date(2023, 1, 10)));

        assertArrayEquals(expected.toArray(), actual.stream().toArray());
    }

    @Test
    public void noConflictTest() {
        final var actual = Series.union(List.of(
                datapoint(1, date(2023, 1, 3), 50),
                end(date(2023, 1, 10))),
                List.of(
                        datapoint(2, date(2023, 1, 15), 100),
                        end(date(2023, 1, 20))

                ), CrdtTest::resolveConflicts);

        final var expected = List.of(
                datapoint(1, date(2023, 1, 3), 50),
                end(date(2023, 1, 10)),
                datapoint(2, date(2023, 1, 15), 100),
                end(date(2023, 1, 20)));
        assertArrayEquals(expected.toArray(), actual.stream().toArray());
    }

    private static record Tuple<L, R>(L fst, R snd) {
    }

    /**
     * Optional from java.util does not provide any Comparable<Optional<T>>
     * implementation like other languages (rust with traits).
     * 
     * This Algebraic data type provides this implementation of a conventional
     * option.
     */
    private static sealed interface Option<T extends Comparable<T>> extends Comparable<Option<T>>
            permits Option.None, Option.Some {
        default int compareTo(final Option<T> o) {
            return switch(new Tuple<>(this, o)){
                case final Tuple<Option<T>, Option<T>> (Option.None<T> fst, Option.None<T> snd) -> 0;
                case final Tuple<Option<T>, Option<T>> (Option.Some<T> fst, Option.None<T> snd) -> 1;
                case final Tuple<Option<T>, Option<T>> (Option.None<T> fst, Option.Some<T> snd) -> -1;
                case final Tuple<Option<T>, Option<T>> (Option.Some<T> fst, Option.Some<T> snd) -> fst.value.compareTo(snd.value);
            };
        }

        static record None<T extends Comparable<T>>() implements Option<T> {
        }

        static record Some<T extends Comparable<T>>(T value) implements Option<T> {

        }

        private static <T extends Comparable<T>> Option<T> none() {
            return new None<>();
        }

        private static <T extends Comparable<T>> Option<T> some(final T value) {
            return new Some<>(value);
        }
    }

    private static record VersionedValue<V extends Comparable<V>, T extends Comparable<T>>(V version, T value)
            implements Comparable<VersionedValue<V, T>> {
        @Override
        public int compareTo(final VersionedValue<V, T> o) {
            var vc = this.version.compareTo(o.version);

            if (vc < 0) {
                return -1;
            }

            if (vc > 0) {
                return 1;
            }

            return this.value.compareTo(o.value);
        }
    }

    private static record Date(Integer year, Integer month, Integer day) implements Comparable<Date> {

        @Override
        public int compareTo(final Date o) {
            if (this.year > o.year) {
                return 1;
            }

            if (this.year < o.year) {
                return -1;
            }

            if (this.month > o.month) {
                return 1;
            }

            if (this.month < o.month) {
                return -1;
            }

            if (this.day > o.day) {
                return 1;
            }

            if (this.day < o.day) {
                return -1;
            }

            return 0;
        }
    }

    private static final Date date(final Integer year, final Integer month, final Integer day) {
        return new Date(year, month, day);
    }

    private static final <T extends Comparable<T>> DataPoint<Date, Option<VersionedValue<Integer, T>>> datapoint(
            final Integer timestampMicros, final Date date, final T data) {
        return Series.datapoint(date, Option.some(new VersionedValue<>(timestampMicros, data)));
    }

    /// Interval can be encoded by using 2 Datapoints with a [`None`] last datapoint
    /// value to mark the end of each interval
    private static final <T extends Comparable<T>> DataPoint<Date, Option<VersionedValue<Integer, T>>> end(
            final Date date) {
        return Series.datapoint(date, Option.none());
    }

    /**
     * Solves conflict by taking always the maximum version
     */
    private static final <T extends Comparable<T>> T resolveConflicts(final UnionResult<T, T> unionResult) {
        return switch (unionResult) {
            case final UnionResult.LeftOnly<T, T> l -> l.left();
            case final UnionResult.RightOnly<T, T> r -> r.right();
            case final UnionResult.Both<T, T> b when b.right().compareTo(b.left()) > 0 -> b.right();
            case final UnionResult.Both<T, T> b -> b.left();
        };
    }
}
