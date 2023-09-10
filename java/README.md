# dataseries

[![License:MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![build](https://github.com/cboudereau/dataseries/workflows/build-java/badge.svg?branch=main&event=push)](https://github.com/cboudereau/dataseries/actions/workflows/build-java.yml?query=event%3Apush+branch%3Amain)
[![codecov](https://codecov.io/gh/cboudereau/dataseries/branch/main/graph/badge.svg?token=UFSTKQG9FY&flag=java)](https://app.codecov.io/gh/cboudereau/dataseries/tree/main/java)
[![maven central](https://img.shields.io/maven-central/v/io.github.cboudereau.dataseries/dataseries.svg)](https://search.maven.org/artifact/io.github.cboudereau.dataseries/dataseries/)
[![javadoc](https://www.javadoc.io/badge/io.github.cboudereau.dataseries/dataseries.svg)](https://www.javadoc.io/doc/io.github.cboudereau.dataseries/dataseries)

data-series functions support for data-series and time-series.

## functions

### union

Continuous time series union between 2 series. Left and right data can be absent (left and right only cases).

```
          1     3     10                 20
    Left: |-----|-----|------------------|-
          130   120   95                 160
                           12     15
   Right:                  |------|--------
                           105    110
          1     3     10   12     15     20
Expected: |-----|-----|----|------|------|-
          130,∅ 120,∅ 95,∅ 95,105 95,110 160,110

```

### examples

#### simple
A simple example of ```union``` between 2 timeseries

```java
package io.github.cboudereau.dataseries.snippets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Series;
import io.github.cboudereau.dataseries.UnionResult;

public class SimpleTest {
    @Test
    public void simple() {
        final var s1 = List.of(Series.datapoint(3, 50));
        final var s2 = List.of(Series.datapoint(4, 100), Series.datapoint(7, 110));

        final var actual = Series.union(s1, s2, x -> x).stream().toArray();

        final var expected = List.of(
                Series.datapoint(3, UnionResult.leftOnly(50)),
                Series.datapoint(4, UnionResult.both(50, 100)),
                Series.datapoint(7, UnionResult.both(50, 110))).toArray();

        assertArrayEquals(expected, actual);
    }
}
```

#### merge
A merge removes duplicates (same contiguous values)

```java
package io.github.cboudereau.dataseries.snippets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Series;

public class MergeTest {
    @Test
    public void contiguousTest() {
        final var s1 = List.of(Series.datapoint(1, 100), Series.datapoint(3, 100));
        final var actual = Series.merge(s1);

        final var expected = List.of(Series.datapoint(1, 100));

        assertArrayEquals(expected.toArray(), actual.stream().toArray());
    }

    @Test
    public void uncontiguousTest() {
        final var s1 = List.of(Series.datapoint(1, 100), Series.datapoint(3, 10));
        final var actual = Series.merge(s1);

        final var expected = List.of(Series.datapoint(1, 100), Series.datapoint(3, 10));

        assertArrayEquals(expected.toArray(), actual.stream().toArray());
    }
}
```

#### intersection
An intersection implementation using the ```union``` function.

```java
package io.github.cboudereau.dataseries.snippets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.cboudereau.dataseries.Series;
import io.github.cboudereau.dataseries.UnionResult;

public class IntersectionTest {
    @Test
    public void intersection() {
        final var s1 = List.of(Series.datapoint(3, 50));
        final var s2 = List.of(Series.datapoint(4, 100), Series.datapoint(7, 110));

        final var actual = Series.union(s1, s2, IntersectionTest::toTuple).stream().filter(x -> x.data().isPresent())
                .map(x -> Series.datapoint(x.point(), x.data().get())).toArray();

        final var expected = List.of(
                Series.datapoint(4, new Tuple<>(50, 100)),
                Series.datapoint(7, new Tuple<>(50, 110))).toArray();

        assertArrayEquals(expected, actual);
    }

    private static record Tuple<L, R>(L fst, R snd) {
    }

    private static <L, R> Optional<Tuple<L, R>> toTuple(UnionResult<L, R> unionResult) {
        return switch (unionResult) {
            case final UnionResult.LeftOnly<L, R> x -> Optional.empty();
            case final UnionResult.RightOnly<L, R> x -> Optional.empty();
            case final UnionResult.Both<L, R> both -> Optional.of(new Tuple<L, R>(both.left(), both.right()));
        };
    }
}
```

### eventual consistency and conflict resolution
The ```crdt``` example provides an example of the conflict-free replicated data type resolution based on data-series ```union```.

The ```VersionedValue``` defines the version (here a timestamp) to solve the conflict by taking the maximum version. The maximum is defined through the ```Comparable``` interface and used inside the given function used by ```union```.

The below example uses TimestampMicros to version the data and solve conflict by taking the highest version of a value.

```java
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
            return switch (this) {
                case final None<T> n1 -> switch (o) {
                    case final None<T> n2 -> 0;
                    case final Some<T> s -> -1;
                };
                case final Some<T> s1 -> switch (o) {
                    case None<T> n -> 1;
                    case Some<T> s2 -> s1.value.compareTo(s2.value);
                };
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
            case final UnionResult.Both<T, T> b -> b.right().compareTo(b.left()) > 0 ? b.right() : b.left();
        };
    }
}
```