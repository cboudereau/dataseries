package com.dataseries;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;

import com.dataseries.Union.UnionResult;

public class UnionTest {
    record Tuple<T1, T2>(T1 fst, T2 snd) {
    }

    private void test(List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected,
            List<DataPoint<Integer, Integer>> left, List<DataPoint<Integer, Integer>> right) {
        test_ex(expected, left, right, true);
    }

    private void test_ex(List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected,
            List<DataPoint<Integer, Integer>> left, List<DataPoint<Integer, Integer>> right, Boolean canMirror) {
        {
            var iterator = new Union<>(right.iterator(), left.iterator(), (x -> x));
            Iterable<DataPoint<Integer, Union.UnionResult<Integer, Integer>>> iterable = () -> iterator;
            var actual = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList()).toArray();
            assertArrayEquals(expected.toArray(), actual);
        }
        if (canMirror) {
            var iterator = new Union<>(left.iterator(), right.iterator(), (x -> x));
            Iterable<DataPoint<Integer, Union.UnionResult<Integer, Integer>>> iterable = () -> iterator;
            var actual = StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList()).toArray();
            var expectedArray = expected.stream().map(x -> switch (x.data()) {
                case UnionResult.LeftOnly<Integer, Integer> leftOnly ->
                    new DataPoint<>(x.point(), UnionResult.rightOnly(leftOnly.left()));
                case UnionResult.RightOnly<Integer, Integer> rightOnly ->
                    new DataPoint<>(x.point(), UnionResult.leftOnly(rightOnly.right()));
                case UnionResult.Both<Integer, Integer> both ->
                    new DataPoint<>(x.point(), UnionResult.both(both.right(), both.left()));
            }).collect(Collectors.toList()).toArray();
            assertArrayEquals(expectedArray, actual);
        }
    }

    @Test
    public void emptyTest() {
        test(Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void singleEmptyTest() {
        List<DataPoint<Integer, Integer>> left = List.of(new DataPoint<>(1, 100));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List
                .of(new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(100)));

        test(expected, left, Collections.emptyList());
    }

    @Test
    public void singlesEmptyTest() {
        List<DataPoint<Integer, Integer>> left = List.of(
                new DataPoint<>(1, 100),
                new DataPoint<>(3, 100),
                new DataPoint<>(4, 100));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(100)),
                new DataPoint<>(3, new UnionResult.LeftOnly<Integer, Integer>(100)),
                new DataPoint<>(4, new UnionResult.LeftOnly<Integer, Integer>(100)));

        test(expected, left, Collections.emptyList());
    }

    @Test
    public void singleSingleTest() {
        List<DataPoint<Integer, Integer>> left = List.of(
                new DataPoint<>(2, 120));
        List<DataPoint<Integer, Integer>> right = List.of(
                new DataPoint<>(1, 100));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.RightOnly<Integer, Integer>(100)),
                new DataPoint<>(2, new UnionResult.Both<Integer, Integer>(120, 100)));

        test(expected, left, right);
    }

    @Test
    public void singleSingleFullOverlapTest() {
        List<DataPoint<Integer, Integer>> left = List.of(
                new DataPoint<>(1, 120));
        List<DataPoint<Integer, Integer>> right = List.of(
                new DataPoint<>(1, 100));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.Both<Integer, Integer>(120, 100)));

        test(expected, left, right);
    }

    @Test
    public void singlePairTest() {
        var left = List.of(new DataPoint<>(10, 130));
        var right = List.of(new DataPoint<>(1, 120), new DataPoint<>(5, 200));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.RightOnly<Integer, Integer>(120)),
                new DataPoint<>(5, new UnionResult.RightOnly<Integer, Integer>(200)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(130, 200)));
        test(expected, left, right);
    }

    @Test
    public void singlePairTest2() {
        var left = List.of(new DataPoint<>(2, 120));
        var right = List.of(new DataPoint<>(1, 100), new DataPoint<>(3, 150));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.RightOnly<Integer, Integer>(100)),
                new DataPoint<>(2, new UnionResult.Both<Integer, Integer>(120, 100)),
                new DataPoint<>(3, new UnionResult.Both<Integer, Integer>(120, 150)));
        test(expected, left, right);
    }

    @Test
    public void singleMultiple3Test() {
        var left = List.of(new DataPoint<>(1, 120));
        var right = List.of(new DataPoint<>(2, 100), new DataPoint<>(5, 150));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(120)),
                new DataPoint<>(2, new UnionResult.Both<Integer, Integer>(120, 100)),
                new DataPoint<>(5, new UnionResult.Both<Integer, Integer>(120, 150)));
        test(expected, left, right);
    }

    @Test
    public void partialIntersectionTest() {
        var left = List.of(new DataPoint<>(1, 130), new DataPoint<>(3, 120), new DataPoint<>(10, 95));
        var right = List.of(new DataPoint<>(2, 120), new DataPoint<>(10, 95));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(2, new UnionResult.Both<Integer, Integer>(130, 120)),
                new DataPoint<>(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(95, 95)));
        test(expected, left, right);
    }

    @Test
    public void segmentedFullIntersectionTest() {
        var left = List.of(new DataPoint<>(1, 130), new DataPoint<>(3, 120), new DataPoint<>(10, 95));
        var right = List.of(new DataPoint<>(3, 120), new DataPoint<>(10, 95));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(95, 95)));
        test(expected, left, right);
    }

    @Test
    public void pairPairTest() {
        var left = List.of(new DataPoint<>(10, 130), new DataPoint<>(12, 140));
        var right = List.of(new DataPoint<>(1, 120), new DataPoint<>(5, 200));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.RightOnly<Integer, Integer>(120)),
                new DataPoint<>(5, new UnionResult.RightOnly<Integer, Integer>(200)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(130, 200)),
                new DataPoint<>(12, new UnionResult.Both<Integer, Integer>(140, 200)));
        test(expected, left, right);
    }

    @Test
    public void multipleFirstTest() {
        var left = List.of(new DataPoint<>(1, 130), new DataPoint<>(2, 140), new DataPoint<>(5, 150),
                new DataPoint<>(20, 160));
        var right = List.of(new DataPoint<>(30, 120));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(2, new UnionResult.LeftOnly<Integer, Integer>(140)),
                new DataPoint<>(5, new UnionResult.LeftOnly<Integer, Integer>(150)),
                new DataPoint<>(20, new UnionResult.LeftOnly<Integer, Integer>(160)),
                new DataPoint<>(30, new UnionResult.Both<Integer, Integer>(160, 120)));
        test(expected, left, right);
    }

    @Test
    public void multipleIntersectionTest() {
        var left = List.of(new DataPoint<>(1, 130), new DataPoint<>(20, 160));
        var right = List.of(new DataPoint<>(3, 120), new DataPoint<>(5, 110), new DataPoint<>(6, 100),
                new DataPoint<>(10, 90), new DataPoint<>(15, 190), new DataPoint<>(19, 180));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(3, new UnionResult.Both<Integer, Integer>(130, 120)),
                new DataPoint<>(5, new UnionResult.Both<Integer, Integer>(130, 110)),
                new DataPoint<>(6, new UnionResult.Both<Integer, Integer>(130, 100)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(130, 90)),
                new DataPoint<>(15, new UnionResult.Both<Integer, Integer>(130, 190)),
                new DataPoint<>(19, new UnionResult.Both<Integer, Integer>(130, 180)),
                new DataPoint<>(20, new UnionResult.Both<Integer, Integer>(160, 180)));
        test(expected, left, right);
    }

    @Test
    public void multipleIntersectionsOverlapTest() {
        var left = List.of(new DataPoint<>(1, 130), new DataPoint<>(20, 160));
        var right = List.of(new DataPoint<>(3, 120), new DataPoint<>(5, 110), new DataPoint<>(6, 100),
                new DataPoint<>(10, 90), new DataPoint<>(15, 190), new DataPoint<>(20, 180));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(3, new UnionResult.Both<Integer, Integer>(130, 120)),
                new DataPoint<>(5, new UnionResult.Both<Integer, Integer>(130, 110)),
                new DataPoint<>(6, new UnionResult.Both<Integer, Integer>(130, 100)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(130, 90)),
                new DataPoint<>(15, new UnionResult.Both<Integer, Integer>(130, 190)),
                new DataPoint<>(20, new UnionResult.Both<Integer, Integer>(160, 180)));
        test(expected, left, right);
    }

    @Test
    public void multipleIntersectionsOverlapsTest() {
        var left = List.of(new DataPoint<>(1, 130), new DataPoint<>(3, 120), new DataPoint<>(10, 95), new DataPoint<>(20, 160));
        var right = List.of(new DataPoint<>(3, 105), new DataPoint<>(5, 110), new DataPoint<>(6, 100),
                new DataPoint<>(10, 90), new DataPoint<>(15, 190), new DataPoint<>(20, 180));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(3, new UnionResult.Both<Integer, Integer>(120, 105)),
                new DataPoint<>(5, new UnionResult.Both<Integer, Integer>(120, 110)),
                new DataPoint<>(6, new UnionResult.Both<Integer, Integer>(120, 100)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(95, 90)),
                new DataPoint<>(15, new UnionResult.Both<Integer, Integer>(95, 190)),
                new DataPoint<>(20, new UnionResult.Both<Integer, Integer>(160, 180)));
        test(expected, left, right);
    }

    @Test
    public void multipleNoIntersectionTest() {
        var left = List.of(new DataPoint<>(1, 130), new DataPoint<>(3, 120), new DataPoint<>(10, 95), new DataPoint<>(20, 160));
        var right = List.of(new DataPoint<>(12, 105), new DataPoint<>(15, 110));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(3, new UnionResult.LeftOnly<Integer, Integer>(120)),
                new DataPoint<>(10, new UnionResult.LeftOnly<Integer, Integer>(95)),
                new DataPoint<>(12, new UnionResult.Both<Integer, Integer>(95, 105)),
                new DataPoint<>(15, new UnionResult.Both<Integer, Integer>(95, 110)),
                new DataPoint<>(20, new UnionResult.Both<Integer, Integer>(160, 110)));
        test(expected, left, right);
    }

    @Test
    public void fullIntersectionTest() {
        var left = List.of(new DataPoint<>(1, 130), new DataPoint<>(3, 120), new DataPoint<>(10, 95), new DataPoint<>(20, 160));
        var right = List.of(new DataPoint<>(1, 130), new DataPoint<>(3, 120), new DataPoint<>(10, 95), new DataPoint<>(20, 160));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(1, new UnionResult.Both<Integer, Integer>(130, 130)),
                new DataPoint<>(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(95, 95)),
                new DataPoint<>(20, new UnionResult.Both<Integer, Integer>(160, 160)));
        test(expected, left, right);
    }

    @Test
    public void fullIntersection2Test() {
        var left = List.of(new DataPoint<>(-15, 130), new DataPoint<>(-1, 130), new DataPoint<>(1, 130), new DataPoint<>(3, 120), new DataPoint<>(10, 95), new DataPoint<>(20, 160));
        var right = List.of(new DataPoint<>(1, 130), new DataPoint<>(3, 120), new DataPoint<>(10, 95), new DataPoint<>(20, 160));
        List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                new DataPoint<>(-15, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(-1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                new DataPoint<>(1, new UnionResult.Both<Integer, Integer>(130, 130)),
                new DataPoint<>(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                new DataPoint<>(10, new UnionResult.Both<Integer, Integer>(95, 95)),
                new DataPoint<>(20, new UnionResult.Both<Integer, Integer>(160, 160)));
        test(expected, left, right);
    }

}
