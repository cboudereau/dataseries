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
                        var union = DataPoints.union(left.iterator(), right.iterator(), (x -> x));
                        var actual = DataPoints.stream(union).toArray();
                        assertArrayEquals(expected.toArray(), actual);
                }
                if (canMirror) {
                        var union = DataPoints.union(right.iterator(), left.iterator(), (x -> x));
                        var actual = DataPoints.stream(union).toArray();

                        var expectedArray = expected.stream().map(x -> switch (x.data()) {
                                case UnionResult.LeftOnly<Integer, Integer> leftOnly ->
                                        DataPoints.datapoint(x.point(), UnionResult.rightOnly(leftOnly.left()));
                                case UnionResult.RightOnly<Integer, Integer> rightOnly ->
                                        DataPoints.datapoint(x.point(), UnionResult.leftOnly(rightOnly.right()));
                                case UnionResult.Both<Integer, Integer> both ->
                                        DataPoints.datapoint(x.point(), UnionResult.both(both.right(), both.left()));
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
                List<DataPoint<Integer, Integer>> left = List.of(DataPoints.datapoint(1, 100));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List
                                .of(DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(100)));

                test(expected, left, Collections.emptyList());
        }

        @Test
        public void singlesEmptyTest() {
                List<DataPoint<Integer, Integer>> left = List.of(
                                DataPoints.datapoint(1, 100),
                                DataPoints.datapoint(3, 100),
                                DataPoints.datapoint(4, 100));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(100)),
                                DataPoints.datapoint(3, new UnionResult.LeftOnly<Integer, Integer>(100)),
                                DataPoints.datapoint(4, new UnionResult.LeftOnly<Integer, Integer>(100)));

                test(expected, left, Collections.emptyList());
        }

        @Test
        public void singleSingleTest() {
                List<DataPoint<Integer, Integer>> left = List.of(
                                DataPoints.datapoint(2, 120));
                List<DataPoint<Integer, Integer>> right = List.of(
                                DataPoints.datapoint(1, 100));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.RightOnly<Integer, Integer>(100)),
                                DataPoints.datapoint(2, new UnionResult.Both<Integer, Integer>(120, 100)));

                test(expected, left, right);
        }

        @Test
        public void singleSingleFullOverlapTest() {
                List<DataPoint<Integer, Integer>> left = List.of(
                                DataPoints.datapoint(1, 120));
                List<DataPoint<Integer, Integer>> right = List.of(
                                DataPoints.datapoint(1, 100));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.Both<Integer, Integer>(120, 100)));

                test(expected, left, right);
        }

        @Test
        public void singlePairTest() {
                var left = List.of(DataPoints.datapoint(10, 130));
                var right = List.of(DataPoints.datapoint(1, 120), DataPoints.datapoint(5, 200));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.RightOnly<Integer, Integer>(120)),
                                DataPoints.datapoint(5, new UnionResult.RightOnly<Integer, Integer>(200)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(130, 200)));
                test(expected, left, right);
        }

        @Test
        public void singlePairTest2() {
                var left = List.of(DataPoints.datapoint(2, 120));
                var right = List.of(DataPoints.datapoint(1, 100), DataPoints.datapoint(3, 150));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.RightOnly<Integer, Integer>(100)),
                                DataPoints.datapoint(2, new UnionResult.Both<Integer, Integer>(120, 100)),
                                DataPoints.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 150)));
                test(expected, left, right);
        }

        @Test
        public void singleMultiple3Test() {
                var left = List.of(DataPoints.datapoint(1, 120));
                var right = List.of(DataPoints.datapoint(2, 100), DataPoints.datapoint(5, 150));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(120)),
                                DataPoints.datapoint(2, new UnionResult.Both<Integer, Integer>(120, 100)),
                                DataPoints.datapoint(5, new UnionResult.Both<Integer, Integer>(120, 150)));
                test(expected, left, right);
        }

        @Test
        public void partialIntersectionTest() {
                var left = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(3, 120),
                                DataPoints.datapoint(10, 95));
                var right = List.of(DataPoints.datapoint(2, 120), DataPoints.datapoint(10, 95));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(2, new UnionResult.Both<Integer, Integer>(130, 120)),
                                DataPoints.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 95)));
                test(expected, left, right);
        }

        @Test
        public void segmentedFullIntersectionTest() {
                var left = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(3, 120),
                                DataPoints.datapoint(10, 95));
                var right = List.of(DataPoints.datapoint(3, 120), DataPoints.datapoint(10, 95));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 95)));
                test(expected, left, right);
        }

        @Test
        public void pairPairTest() {
                var left = List.of(DataPoints.datapoint(10, 130), DataPoints.datapoint(12, 140));
                var right = List.of(DataPoints.datapoint(1, 120), DataPoints.datapoint(5, 200));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.RightOnly<Integer, Integer>(120)),
                                DataPoints.datapoint(5, new UnionResult.RightOnly<Integer, Integer>(200)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(130, 200)),
                                DataPoints.datapoint(12, new UnionResult.Both<Integer, Integer>(140, 200)));
                test(expected, left, right);
        }

        @Test
        public void multipleFirstTest() {
                var left = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(2, 140),
                                DataPoints.datapoint(5, 150),
                                DataPoints.datapoint(20, 160));
                var right = List.of(DataPoints.datapoint(30, 120));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(2, new UnionResult.LeftOnly<Integer, Integer>(140)),
                                DataPoints.datapoint(5, new UnionResult.LeftOnly<Integer, Integer>(150)),
                                DataPoints.datapoint(20, new UnionResult.LeftOnly<Integer, Integer>(160)),
                                DataPoints.datapoint(30, new UnionResult.Both<Integer, Integer>(160, 120)));
                test(expected, left, right);
        }

        @Test
        public void multipleIntersectionTest() {
                var left = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(20, 160));
                var right = List.of(DataPoints.datapoint(3, 120), DataPoints.datapoint(5, 110),
                                DataPoints.datapoint(6, 100),
                                DataPoints.datapoint(10, 90), DataPoints.datapoint(15, 190),
                                DataPoints.datapoint(19, 180));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(3, new UnionResult.Both<Integer, Integer>(130, 120)),
                                DataPoints.datapoint(5, new UnionResult.Both<Integer, Integer>(130, 110)),
                                DataPoints.datapoint(6, new UnionResult.Both<Integer, Integer>(130, 100)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(130, 90)),
                                DataPoints.datapoint(15, new UnionResult.Both<Integer, Integer>(130, 190)),
                                DataPoints.datapoint(19, new UnionResult.Both<Integer, Integer>(130, 180)),
                                DataPoints.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 180)));
                test(expected, left, right);
        }

        @Test
        public void multipleIntersectionsOverlapTest() {
                var left = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(20, 160));
                var right = List.of(DataPoints.datapoint(3, 120), DataPoints.datapoint(5, 110),
                                DataPoints.datapoint(6, 100),
                                DataPoints.datapoint(10, 90), DataPoints.datapoint(15, 190),
                                DataPoints.datapoint(20, 180));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(3, new UnionResult.Both<Integer, Integer>(130, 120)),
                                DataPoints.datapoint(5, new UnionResult.Both<Integer, Integer>(130, 110)),
                                DataPoints.datapoint(6, new UnionResult.Both<Integer, Integer>(130, 100)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(130, 90)),
                                DataPoints.datapoint(15, new UnionResult.Both<Integer, Integer>(130, 190)),
                                DataPoints.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 180)));
                test(expected, left, right);
        }

        @Test
        public void multipleIntersectionsOverlapsTest() {
                var left = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(3, 120),
                                DataPoints.datapoint(10, 95),
                                DataPoints.datapoint(20, 160));
                var right = List.of(DataPoints.datapoint(3, 105), DataPoints.datapoint(5, 110),
                                DataPoints.datapoint(6, 100),
                                DataPoints.datapoint(10, 90), DataPoints.datapoint(15, 190),
                                DataPoints.datapoint(20, 180));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 105)),
                                DataPoints.datapoint(5, new UnionResult.Both<Integer, Integer>(120, 110)),
                                DataPoints.datapoint(6, new UnionResult.Both<Integer, Integer>(120, 100)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 90)),
                                DataPoints.datapoint(15, new UnionResult.Both<Integer, Integer>(95, 190)),
                                DataPoints.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 180)));
                test(expected, left, right);
        }

        @Test
        public void multipleNoIntersectionTest() {
                var left = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(3, 120),
                                DataPoints.datapoint(10, 95),
                                DataPoints.datapoint(20, 160));
                var right = List.of(DataPoints.datapoint(12, 105), DataPoints.datapoint(15, 110));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(3, new UnionResult.LeftOnly<Integer, Integer>(120)),
                                DataPoints.datapoint(10, new UnionResult.LeftOnly<Integer, Integer>(95)),
                                DataPoints.datapoint(12, new UnionResult.Both<Integer, Integer>(95, 105)),
                                DataPoints.datapoint(15, new UnionResult.Both<Integer, Integer>(95, 110)),
                                DataPoints.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 110)));
                test(expected, left, right);
        }

        @Test
        public void fullIntersectionTest() {
                var left = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(3, 120),
                                DataPoints.datapoint(10, 95),
                                DataPoints.datapoint(20, 160));
                var right = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(3, 120),
                                DataPoints.datapoint(10, 95),
                                DataPoints.datapoint(20, 160));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(1, new UnionResult.Both<Integer, Integer>(130, 130)),
                                DataPoints.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 95)),
                                DataPoints.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 160)));
                test(expected, left, right);
        }

        @Test
        public void fullIntersection2Test() {
                var left = List.of(DataPoints.datapoint(-15, 130), DataPoints.datapoint(-1, 130),
                                DataPoints.datapoint(1, 130),
                                DataPoints.datapoint(3, 120), DataPoints.datapoint(10, 95),
                                DataPoints.datapoint(20, 160));
                var right = List.of(DataPoints.datapoint(1, 130), DataPoints.datapoint(3, 120),
                                DataPoints.datapoint(10, 95),
                                DataPoints.datapoint(20, 160));
                List<DataPoint<Integer, UnionResult<Integer, Integer>>> expected = List.of(
                                DataPoints.datapoint(-15, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(-1, new UnionResult.LeftOnly<Integer, Integer>(130)),
                                DataPoints.datapoint(1, new UnionResult.Both<Integer, Integer>(130, 130)),
                                DataPoints.datapoint(3, new UnionResult.Both<Integer, Integer>(120, 120)),
                                DataPoints.datapoint(10, new UnionResult.Both<Integer, Integer>(95, 95)),
                                DataPoints.datapoint(20, new UnionResult.Both<Integer, Integer>(160, 160)));
                test(expected, left, right);
        }

}
