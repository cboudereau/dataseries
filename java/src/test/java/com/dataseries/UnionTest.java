package com.dataseries;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class UnionTest {
    record Tuple<T1, T2>(T1 fst, T2 snd) {
    }

    private static void test(List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected,
            List<DataPoint<Integer, Integer>> left, List<DataPoint<Integer, Integer>> right) {
        test_ex(expected, left, right, true);
    }

    private static void test_ex(List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected,
            List<DataPoint<Integer, Integer>> left, List<DataPoint<Integer, Integer>> right,
            Boolean canMirror) {
        {
            var union = Series.union(left, right, (x -> x));
            var actual = union.stream().toArray();
            assertArrayEquals(expected.toArray(), actual);
        }
        if (canMirror) {
            var union = Series.union(right, left, (x -> x));
            var actual = union.stream().toArray();

            var expectedArray = expected.stream().map(x -> switch (x.data()) {
                case UnionResult.LeftOnly<Integer, Integer> leftOnly ->
                    Series.datapoint(x.point(), UnionResult.rightOnly(leftOnly.left()));
                case UnionResult.RightOnly<Integer, Integer> rightOnly ->
                    Series.datapoint(x.point(), UnionResult.leftOnly(rightOnly.right()));
                case UnionResult.Both<Integer, Integer> both ->
                    Series.datapoint(x.point(), UnionResult.both(both.right(), both.left()));
            }).toArray();
            assertArrayEquals(expectedArray, actual);
        }
    }

    @Test
    public void emptyTest() {
        test(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void singleEmptyTest() {
        List<DataPoint<Integer, Integer>> left = List.of(Series.datapoint(1, 100));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List
                .of(Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(100)));

        test(expected, left, Collections.emptyList());
    }

    @Test
    public void singlesEmptyTest() {
        List<DataPoint<Integer, Integer>> left = List.of(
                Series.datapoint(1, 100),
                Series.datapoint(3, 100),
                Series.datapoint(4, 100));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(100)),
                Series.datapoint(3, new UnionResult.LeftOnly<Integer, Integer>(100)),
                Series.datapoint(4, new UnionResult.LeftOnly<Integer, Integer>(100)));

        test(expected, left, Collections.emptyList());
    }

    @Test
    public void singleSingleTest() {
        List<DataPoint<Integer, Integer>> left = List.of(
                Series.datapoint(2, 120));
        List<DataPoint<Integer, Integer>> right = List.of(
                Series.datapoint(1, 100));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.RightOnly<Integer, Integer>(100)),
                Series.datapoint(2, new UnionResult.Both<Integer, Integer>(120, 100)));

        test(expected, left, right);
    }

    @Test
    public void singleSingleFullOverlapTest() {
        List<DataPoint<Integer, Integer>> left = List.of(
                Series.datapoint(1, 120));
        List<DataPoint<Integer, Integer>> right = List.of(
                Series.datapoint(1, 100));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.Both<Integer, Integer>(120, 100)));

        test(expected, left, right);
    }

    @Test
    public void singlePairTest() {
        var left = List.of(Series.datapoint(10, 130));
        var right = List.of(Series.datapoint(1, 120), Series.datapoint(5, 200));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.RightOnly<Integer, Integer>(120)),
                Series.datapoint(5, new UnionResult.RightOnly<Integer, Integer>(200)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(130, 200)));
        test(expected, left, right);
    }

    @Test
    public void singlePairTest2() {
        var left = List.of(Series.datapoint(2, 120));
        var right = List.of(Series.datapoint(1, 100), Series.datapoint(3, 150));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.RightOnly<Integer, Integer>(100)),
                Series.datapoint(2, new UnionResult.Both<Integer, Integer>(120, 100)),
                Series.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 150)));
        test(expected, left, right);
    }

    @Test
    public void singleMultiple3Test() {
        var left = List.of(Series.datapoint(1, 120));
        var right = List.of(Series.datapoint(2, 100), Series.datapoint(5, 150));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(120)),
                Series.datapoint(2, new UnionResult.Both<Integer, Integer>(120, 100)),
                Series.datapoint(5, new UnionResult.Both<Integer, Integer>(120, 150)));
        test(expected, left, right);
    }

    @Test
    public void partialIntersectionTest() {
        var left = List.of(Series.datapoint(1, 130), Series.datapoint(3, 120),
                Series.datapoint(10, 95));
        var right = List.of(Series.datapoint(2, 120), Series.datapoint(10, 95));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(2, new UnionResult.Both<Integer, Integer>(130, 120)),
                Series.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 95)));
        test(expected, left, right);
    }

    @Test
    public void segmentedFullIntersectionTest() {
        var left = List.of(Series.datapoint(1, 130), Series.datapoint(3, 120),
                Series.datapoint(10, 95));
        var right = List.of(Series.datapoint(3, 120), Series.datapoint(10, 95));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 95)));
        test(expected, left, right);
    }

    @Test
    public void pairPairTest() {
        var left = List.of(Series.datapoint(10, 130), Series.datapoint(12, 140));
        var right = List.of(Series.datapoint(1, 120), Series.datapoint(5, 200));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.RightOnly<Integer, Integer>(120)),
                Series.datapoint(5, new UnionResult.RightOnly<Integer, Integer>(200)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(130, 200)),
                Series.datapoint(12, new UnionResult.Both<Integer, Integer>(140, 200)));
        test(expected, left, right);
    }

    @Test
    public void multipleFirstTest() {
        var left = List.of(Series.datapoint(1, 130), Series.datapoint(2, 140),
                Series.datapoint(5, 150),
                Series.datapoint(20, 160));
        var right = List.of(Series.datapoint(30, 120));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(2, new UnionResult.LeftOnly<Integer, Integer>(140)),
                Series.datapoint(5, new UnionResult.LeftOnly<Integer, Integer>(150)),
                Series.datapoint(20, new UnionResult.LeftOnly<Integer, Integer>(160)),
                Series.datapoint(30, new UnionResult.Both<Integer, Integer>(160, 120)));
        test(expected, left, right);
    }

    @Test
    public void multipleIntersectionTest() {
        var left = List.of(Series.datapoint(1, 130), Series.datapoint(20, 160));
        var right = List.of(Series.datapoint(3, 120), Series.datapoint(5, 110),
                Series.datapoint(6, 100),
                Series.datapoint(10, 90), Series.datapoint(15, 190),
                Series.datapoint(19, 180));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(3, new UnionResult.Both<Integer, Integer>(130, 120)),
                Series.datapoint(5, new UnionResult.Both<Integer, Integer>(130, 110)),
                Series.datapoint(6, new UnionResult.Both<Integer, Integer>(130, 100)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(130, 90)),
                Series.datapoint(15, new UnionResult.Both<Integer, Integer>(130, 190)),
                Series.datapoint(19, new UnionResult.Both<Integer, Integer>(130, 180)),
                Series.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 180)));
        test(expected, left, right);
    }

    @Test
    public void multipleIntersectionsOverlapTest() {
        var left = List.of(Series.datapoint(1, 130), Series.datapoint(20, 160));
        var right = List.of(Series.datapoint(3, 120), Series.datapoint(5, 110),
                Series.datapoint(6, 100),
                Series.datapoint(10, 90), Series.datapoint(15, 190),
                Series.datapoint(20, 180));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(3, new UnionResult.Both<Integer, Integer>(130, 120)),
                Series.datapoint(5, new UnionResult.Both<Integer, Integer>(130, 110)),
                Series.datapoint(6, new UnionResult.Both<Integer, Integer>(130, 100)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(130, 90)),
                Series.datapoint(15, new UnionResult.Both<Integer, Integer>(130, 190)),
                Series.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 180)));
        test(expected, left, right);
    }

    @Test
    public void multipleIntersectionsOverlapsTest() {
        var left = List.of(Series.datapoint(1, 130), Series.datapoint(3, 120),
                Series.datapoint(10, 95),
                Series.datapoint(20, 160));
        var right = List.of(Series.datapoint(3, 105), Series.datapoint(5, 110),
                Series.datapoint(6, 100),
                Series.datapoint(10, 90), Series.datapoint(15, 190),
                Series.datapoint(20, 180));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 105)),
                Series.datapoint(5, new UnionResult.Both<Integer, Integer>(120, 110)),
                Series.datapoint(6, new UnionResult.Both<Integer, Integer>(120, 100)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 90)),
                Series.datapoint(15, new UnionResult.Both<Integer, Integer>(95, 190)),
                Series.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 180)));
        test(expected, left, right);
    }

    @Test
    public void multipleNoIntersectionTest() {
        var left = List.of(Series.datapoint(1, 130), Series.datapoint(3, 120),
                Series.datapoint(10, 95),
                Series.datapoint(20, 160));
        var right = List.of(Series.datapoint(12, 105), Series.datapoint(15, 110));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(3, new UnionResult.LeftOnly<Integer, Integer>(120)),
                Series.datapoint(10, new UnionResult.LeftOnly<Integer, Integer>(95)),
                Series.datapoint(12, new UnionResult.Both<Integer, Integer>(95, 105)),
                Series.datapoint(15, new UnionResult.Both<Integer, Integer>(95, 110)),
                Series.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 110)));
        test(expected, left, right);
    }

    @Test
    public void fullIntersectionTest() {
        var left = List.of(Series.datapoint(1, 130), Series.datapoint(3, 120),
                Series.datapoint(10, 95),
                Series.datapoint(20, 160));
        var right = List.of(Series.datapoint(1, 130), Series.datapoint(3, 120),
                Series.datapoint(10, 95),
                Series.datapoint(20, 160));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(1, new UnionResult.Both<Integer, Integer>(130, 130)),
                Series.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 95)),
                Series.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 160)));
        test(expected, left, right);
    }

    @Test
    public void fullIntersection2Test() {
        var left = List.of(Series.datapoint(-15, 130), Series.datapoint(-1, 130),
                Series.datapoint(1, 130),
                Series.datapoint(3, 120), Series.datapoint(10, 95),
                Series.datapoint(20, 160));
        var right = List.of(Series.datapoint(1, 130), Series.datapoint(3, 120),
                Series.datapoint(10, 95),
                Series.datapoint(20, 160));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                Series.datapoint(-15, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(-1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                Series.datapoint(1, new UnionResult.Both<Integer, Integer>(130, 130)),
                Series.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                Series.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 95)),
                Series.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 160)));
        test(expected, left, right);
    }

}
