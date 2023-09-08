package io.github.cboudereau.dataseries;

import java.util.function.Function;

/**
 * The entrypoint of the api, here is a simple usage of the union dataseries :
 * 
 * <code>
 * <br/>
 * <br/>
 * package io.github.cboudereau.dataseries.snippets;<br/>
 * <br/>
 * import static org.junit.jupiter.api.Assertions.assertArrayEquals;<br/>
 * <br/>
 * import java.util.List;<br/>
 * import org.junit.jupiter.api.Test;<br/>
 * <br/>
 * import io.github.cboudereau.dataseries.Series;<br/>
 * import io.github.cboudereau.dataseries.UnionResult;<br/>
 * <br/>
 * public class SimpleTest {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&#64;Test<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;public void simple() {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var s1 = List.of(Series.datapoint(3, 50));<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var s2 = List.of(Series.datapoint(4, 100), Series.datapoint(7, 110));<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var actual = Series.union(s1, s2, x -> x).stream().toArray();<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var expected = List.of(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Series.datapoint(3, UnionResult.leftOnly(50)),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Series.datapoint(4, UnionResult.both(50, 100)),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Series.datapoint(7, UnionResult.both(50, 110))).toArray();<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assertArrayEquals(expected, actual);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * }<br/>
 * </code>
 * <br/>
 * Remove duplicates from contiguous data
 * <br/>
 * <br/>
 * <code>
 * package io.github.cboudereau.dataseries.snippets;<br/>
 * <br/>
 * import static org.junit.jupiter.api.Assertions.assertArrayEquals;<br/>
 * <br/>
 * import java.util.List;<br/>
 * <br/>
 * import org.junit.jupiter.api.Test;<br/>
 * <br/>
 * import io.github.cboudereau.dataseries.Series;<br/>
 * <br/>
 * public class MergeTest {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&#64;Test<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;public void contiguousTest() {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var s1 = List.of(Series.datapoint(1, 100), Series.datapoint(3, 100));<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var actual = Series.merge(s1);<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var expected = List.of(Series.datapoint(1, 100));<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assertArrayEquals(expected.toArray(), actual.stream().toArray());<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&#64;Test<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;public void uncontiguousTest() {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var s1 = List.of(Series.datapoint(1, 100), Series.datapoint(3, 10));<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var actual = Series.merge(s1);<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var expected = List.of(Series.datapoint(1, 100), Series.datapoint(3, 10));<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assertArrayEquals(expected.toArray(), actual.stream().toArray());<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * }<br/>
 * </code>
 * <br/>
 * <br/>
 * <br/>
 * Convert an Union to an Intersection example :
 * <code>
 * <br/>
 * <br/>
 * package io.github.cboudereau.dataseries.snippets;<br/>
 * <br/>
 * import static org.junit.jupiter.api.Assertions.assertArrayEquals;<br/>
 * import java.util.List;<br/>
 * import java.util.Optional;<br/>
 * <br/>
 * import org.junit.jupiter.api.Test;<br/>
 * <br/>
 * import io.github.cboudereau.dataseries.Series;<br/>
 * import io.github.cboudereau.dataseries.UnionResult;<br/>
 * <br/>
 * public class IntersectionTest {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&#64;Test<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;public void intersection() {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var s1 = List.of(Series.datapoint(3, 50));<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var s2 = List.of(Series.datapoint(4, 100), Series.datapoint(7, 110));<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var actual = Series.union(s1, s2, IntersectionTest::toTuple).stream().filter(x -&#62; x.data().isPresent())<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;.map(x -&#62; Series.datapoint(x.point(), x.data().get())).toArray();<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var expected = List.of(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Series.datapoint(4, new Tuple&#60;&#62;(50, 100)),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Series.datapoint(7, new Tuple&#60;&#62;(50, 110))).toArray();<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assertArrayEquals(expected, actual);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static record Tuple&#60;L, R&#62;(L fst, R snd) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static &#60;L, R&#62; Optional&#60;Tuple&#60;L, R&#62;&#62; toTuple(UnionResult&#60;L, R&#62; unionResult) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return switch (unionResult) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case UnionResult.LeftOnly&#60;L, R&#62; x -&#62; Optional.empty();<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case UnionResult.RightOnly&#60;L, R&#62; x -&#62; Optional.empty();<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case UnionResult.Both&#60;L, R&#62; both -&#62; Optional.of(new Tuple&#60;L, R&#62;(both.left(), both.right()));<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;};<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * } 
 * <br/>
 * </code>
 * <br/>
 * 
 * And a more complex example using crdt strategy to merge conflicts between 2
 * dataseries by using union
 * 
 * 
 *
 * <code>
 * <br/>
 * <br/>
 * package io.github.cboudereau.dataseries.snippets;<br/>
 * <br/>
 * import static org.junit.jupiter.api.Assertions.assertArrayEquals;<br/>
 * <br/>
 * import java.util.List;<br/>
 * <br/>
 * import org.junit.jupiter.api.Test;<br/>
 * <br/>
 * import io.github.cboudereau.dataseries.DataPoint;<br/>
 * import io.github.cboudereau.dataseries.Series;<br/>
 * import io.github.cboudereau.dataseries.UnionResult;<br/>
 * <br/>
 * public class CrdtTest {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&#64;Test<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;public void resolveConflictsTest() {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var actual = Series.union(List.of(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(1, date(2023, 1, 3), 50),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;end(date(2023, 1, 10))),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;List.of(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(2, date(2023, 1, 4), 100),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;end(date(2023, 1, 5)),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(2, date(2023, 1, 7), 110),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;end(date(2023, 1, 9))),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CrdtTest::resolveConflicts);<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var expected = List.of(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(1, date(2023, 1, 3), 50),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(2, date(2023, 1, 4), 100),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(1, date(2023, 1, 5), 50),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(2, date(2023, 1, 7), 110),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(1, date(2023, 1, 9), 50),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;end(date(2023, 1, 10)));<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assertArrayEquals(expected.toArray(), actual.stream().toArray());<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&#64;Test<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;public void noConflictTest() {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var actual = Series.union(List.of(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(1, date(2023, 1, 3), 50),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;end(date(2023, 1, 10))),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;List.of(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(2, date(2023, 1, 15), 100),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;end(date(2023, 1, 20))<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;), CrdtTest::resolveConflicts);<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final var expected = List.of(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(1, date(2023, 1, 3), 50),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;end(date(2023, 1, 10)),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;datapoint(2, date(2023, 1, 15), 100),<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;end(date(2023, 1, 20)));<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;assertArrayEquals(expected.toArray(), actual.stream().toArray());<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&#47;**<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; * Optional from java.util does not provide any Comparable&#60;Optional&#60;T&#62;&#62;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; * implementation like other languages (rust with traits).<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; * This Algebraic data type provides this implementation of a conventional<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; * option.<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; *&#47;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static sealed interface Option&#60;T extends Comparable&#60;T&#62;&#62; extends Comparable&#60;Option&#60;T&#62;&#62;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;permits Option.None, Option.Some {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;default int compareTo(final Option&#60;T&#62; o) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return switch (this) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case final None&#60;T&#62; n1 -&#62; switch (o) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case final None&#60;T&#62; n2 -&#62; 0;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case final Some&#60;T&#62; s -&#62; -1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;};<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case final Some&#60;T&#62; s1 -&#62; switch (o) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case None&#60;T&#62; n -&#62; 1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case Some&#60;T&#62; s2 -&#62; s1.value.compareTo(s2.value);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;};<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;};<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;static record None&#60;T extends Comparable&#60;T&#62;&#62;() implements Option&#60;T&#62; {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;static record Some&#60;T extends Comparable&#60;T&#62;&#62;(T value) implements Option&#60;T&#62; {<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;private static &#60;T extends Comparable&#60;T&#62;&#62; Option&#60;T&#62; none() {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return new None&#60;&#62;();<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;private static &#60;T extends Comparable&#60;T&#62;&#62; Option&#60;T&#62; some(final T value) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return new Some&#60;&#62;(value);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static record VersionedValue&#60;V extends Comparable&#60;V&#62;, T extends Comparable&#60;T&#62;&#62;(V version, T value)<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;implements Comparable&#60;VersionedValue&#60;V, T&#62;&#62; {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&#64;Override<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public int compareTo(final VersionedValue&#60;V, T&#62; o) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;var vc = this.version.compareTo(o.version);<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (vc &#60; 0) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return -1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (vc &#62; 0) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return 1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return this.value.compareTo(o.value);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static record Date(Integer year, Integer month, Integer day) implements Comparable&#60;Date&#62; {<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&#64;Override<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;public int compareTo(final Date o) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (this.year &#62; o.year) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return 1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (this.year &#60; o.year) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return -1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (this.month &#62; o.month) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return 1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (this.month &#60; o.month) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return -1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (this.day &#62; o.day) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return 1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;if (this.day &#60; o.day) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return -1;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return 0;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static final Date date(final Integer year, final Integer month, final Integer day) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return new Date(year, month, day);<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static final &#60;T extends Comparable&#60;T&#62;&#62; DataPoint&#60;Date, Option&#60;VersionedValue&#60;Integer, T&#62;&#62;&#62; datapoint(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final Integer timestampMicros, final Date date, final T data) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return Series.datapoint(date, Option.some(new VersionedValue&#60;&#62;(timestampMicros, data)));<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;/// Interval can be encoded by using 2 Datapoints with a [`None`] last datapoint<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;/// value to mark the end of each interval<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static final &#60;T extends Comparable&#60;T&#62;&#62; DataPoint&#60;Date, Option&#60;VersionedValue&#60;Integer, T&#62;&#62;&#62; end(<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;final Date date) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return Series.datapoint(date, Option.none());<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * <br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&#47;**<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; * Solves conflict by taking always the maximum version<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp; *&#47;<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;private static final &#60;T extends Comparable&#60;T&#62;&#62; T resolveConflicts(final UnionResult&#60;T, T&#62; unionResult) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;return switch (unionResult) {<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case final UnionResult.LeftOnly&#60;T, T&#62; l -&#62; l.left();<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case final UnionResult.RightOnly&#60;T, T&#62; r -&#62; r.right();<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;case final UnionResult.Both&#60;T, T&#62; b -&#62; b.right().compareTo(b.left()) &#62; 0 ? b.right() : b.left();<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;};<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;}<br/>
 * }<br/>
 * <br/>
 * </code>
 * <br/>
 */
public class Series {
    private Series() {

    }

    /**
     * a helper function to create a datapoint
     * 
     * @param <P>   the point type
     * @param <T>   the data type
     * @param point the point
     * @param data  the data
     * @return a datapoint
     */
    public static final <P extends Comparable<P>, T> DataPoint<P, T> datapoint(final P point, final T data) {
        return new DataPoint<>(point, data);
    }

    /**
     * union 2 series and combine union result with the given function
     * 
     * @param <P>   the point type should be common for left and right series
     * @param <L>   the left type
     * @param <R>   the right type
     * @param <T>   the return of the applied function to union result
     * @param left  the left serie
     * @param right the right serie
     * @param f     the function applied to convert union result to T type
     * @return a iterable series
     */
    public static final <P extends Comparable<P>, L, R, T> IterableSeries<P, T> union(
            final Iterable<DataPoint<P, L>> left, final Iterable<DataPoint<P, R>> right,
            final Function<UnionResult<L, R>, T> f) {
        return () -> new Union<>(left.iterator(), right.iterator(), f);
    }

    /**
     * merge a serie to be more compact when contigous events have the same data
     * 
     * @param <P>    the point type
     * @param <T>    the data type
     * @param series the series to merge
     * @return a merged series which have no more duplicated events for the same
     *         data
     */
    public static final <P, T> IterableSeries<P, T> merge(
            final Iterable<DataPoint<P, T>> series) {
        return () -> new Merge<>(series.iterator());
    }
}
