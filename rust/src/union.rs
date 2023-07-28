use std::cmp::{max, min, Ordering};

use super::{DataPoint, Series};

enum CursorState<T> {
    NotPulled,
    Entry { current: T },
    Done,
}

#[derive(Clone, Copy, Debug, PartialEq)]
enum Cursor<T> {
    Single(T),
    Pair { fst: T, snd: T },
}

#[derive(Debug, PartialEq, Eq, PartialOrd, Ord)]
enum Value<T> {
    Value(T),
    Infinite,
}

impl<T> Cursor<T> {
    fn fst(&self) -> &T {
        match self {
            Self::Single(v) | Self::Pair { fst: v, snd: _ } => v,
        }
    }

    fn can_overlap(&self, other: &Self) -> bool
    where
        T: Ord,
    {
        let fst = max(self.fst(), other.fst());
        let snd = min(self.snd(), other.snd());
        snd > Value::Value(fst)
    }

    fn map<'a, F, U>(&'a self, f: F) -> Cursor<&'a U>
    where
        F: Fn(&'a T) -> &'a U,
    {
        match self {
            Cursor::Single(v) => Cursor::Single(f(v)),
            Cursor::Pair { fst, snd } => Cursor::Pair {
                fst: f(fst),
                snd: f(snd),
            },
        }
    }

    fn snd(&self) -> Value<&T> {
        match self {
            Self::Single(_) => Value::Infinite,
            Self::Pair { fst: _, snd: v } => Value::Value(v),
        }
    }
}

struct CursorIterator<IT>
where
    IT: Iterator,
{
    iterator: IT,
    state: CursorState<IT::Item>,
}

impl<IT> CursorIterator<IT>
where
    IT: Iterator,
{
    fn new(iterator: IT) -> Self {
        Self {
            iterator,
            state: CursorState::NotPulled,
        }
    }
}

impl<IT> Iterator for CursorIterator<IT>
where
    IT: Iterator,
    IT::Item: Copy,
{
    type Item = Cursor<IT::Item>;

    fn next(&mut self) -> Option<Self::Item> {
        match self.state {
            CursorState::Done => None,

            CursorState::NotPulled => match self.iterator.next() {
                Some(current) => match self.iterator.next() {
                    Some(next) => {
                        let value = CursorState::Entry { current: next };
                        self.state = value;
                        Some(Cursor::Pair {
                            fst: current,
                            snd: next,
                        })
                    }
                    None => {
                        self.state = CursorState::Done;
                        Some(Cursor::Single(current))
                    }
                },
                None => {
                    self.state = CursorState::Done;
                    None
                }
            },

            CursorState::Entry { current } => match self.iterator.next() {
                None => {
                    self.state = CursorState::Done;
                    Some(Cursor::Single(current))
                }
                Some(next) => {
                    self.state = CursorState::Entry { current: next };

                    Some(Cursor::Pair {
                        fst: current,
                        snd: next,
                    })
                }
            },
        }
    }
}

pub struct Union<P, L, R, F>
where
    R: Series<Point = P>,
    L: Series<Point = P>,
{
    left: CursorIterator<L>,
    right: CursorIterator<R>,
    f: F,
    state: UnionState<L::Item, R::Item>,
}

impl<P, L, R, F> Union<P, L, R, F>
where
    R: Series<Point = P>,
    L: Series<Point = P>,
{
    pub(crate) fn new(left: L, right: R, f: F) -> Self {
        Self {
            left: CursorIterator::new(left),
            right: CursorIterator::new(right),
            f,
            state: UnionState::default(),
        }
    }
}

#[derive(Default)]
enum UnionState<L, R> {
    #[default]
    None,
    LeftOnly(Cursor<L>),
    RightOnly(Cursor<R>),
    Disjointed {
        left: Cursor<L>,
        right: Cursor<R>,
    },
    Overlapped {
        left: Cursor<L>,
        right: Cursor<R>,
    },
}

pub enum UnionResult<L, R> {
    LeftOnly(L),
    RightOnly(R),
    Union { left: L, right: R },
}

impl<L, R, F, T, P> Iterator for Union<P, L, R, F>
where
    R: Series<Point = P>,
    L: Series<Point = P>,
    P: Copy + Ord + std::fmt::Debug,
    R::Value: Copy + std::fmt::Debug,
    L::Value: Copy + std::fmt::Debug,
    F: Fn(UnionResult<L::Value, R::Value>) -> T,
{
    type Item = DataPoint<L::Point, T>;

    fn next(&mut self) -> Option<Self::Item> {
        let get_union_state =
            |left: Cursor<DataPoint<P, L::Value>>, right: Cursor<DataPoint<P, R::Value>>| {
                if left.fst().point() == right.fst().point() {
                    UnionState::Overlapped { left, right }
                } else {
                    UnionState::Disjointed { left, right }
                }
            };

        self.state = {
            match &self.state {
                UnionState::None => match (self.left.next(), self.right.next()) {
                    (None, None) => None,
                    (Some(left), None) => Some(UnionState::LeftOnly(left)),
                    (None, Some(right)) => Some(UnionState::RightOnly(right)),
                    (Some(left), Some(right)) => Some(get_union_state(left, right)),
                },
                UnionState::Overlapped { left, right } => match left
                    .map(|x| x.point())
                    .snd()
                    .cmp(&right.map(|x| x.point()).snd())
                {
                    Ordering::Less => self.left.next().map(|left| UnionState::Overlapped {
                        left,
                        right: right.to_owned(),
                    }),
                    Ordering::Greater => self.right.next().map(|right| UnionState::Overlapped {
                        left: left.to_owned(),
                        right,
                    }),
                    Ordering::Equal => self.left.next().and_then(|left| {
                        self.right
                            .next()
                            .map(|right| UnionState::Overlapped { left, right })
                    }),
                },
                UnionState::RightOnly(_) => self.right.next().map(UnionState::RightOnly),

                UnionState::LeftOnly(_) => self.left.next().map(UnionState::LeftOnly),
                UnionState::Disjointed { left, right }
                    if left
                        .map(|x| x.point())
                        .can_overlap(&right.map(|x| x.point())) =>
                {
                    Some(UnionState::Overlapped {
                        left: left.to_owned(),
                        right: right.to_owned(),
                    })
                }
                UnionState::Disjointed { left, right } => {
                    if left.map(|x| x.point()).snd() < right.map(|x| x.point()).snd() {
                        self.left
                            .next()
                            .map(|left| get_union_state(left, right.to_owned()))
                    } else {
                        self.right
                            .next()
                            .map(|right| get_union_state(left.to_owned(), right))
                    }
                }
            }
        }
        .unwrap_or(UnionState::None);

        match &self.state {
            UnionState::None => None,
            UnionState::LeftOnly(left) => {
                let left = left.fst();
                Some(DataPoint::new(
                    left.point().to_owned(),
                    (self.f)(UnionResult::LeftOnly(left.data().to_owned())),
                ))
            }
            UnionState::RightOnly(right) => {
                let right = right.fst();
                Some(DataPoint::new(
                    right.point().to_owned(),
                    (self.f)(UnionResult::RightOnly(right.data().to_owned())),
                ))
            }
            UnionState::Disjointed { left, right } => {
                let left = left.fst();
                let right = right.fst();
                if left.point() < right.point() {
                    Some(DataPoint::new(
                        left.point().to_owned(),
                        (self.f)(UnionResult::LeftOnly(left.data().to_owned())),
                    ))
                } else {
                    Some(DataPoint::new(
                        right.point().to_owned(),
                        (self.f)(UnionResult::RightOnly(right.data().to_owned())),
                    ))
                }
            }
            UnionState::Overlapped { left, right } => {
                let left = left.fst();
                let right = right.fst();
                Some(DataPoint::new(
                    max(left.point(), right.point()).to_owned(),
                    (self.f)(UnionResult::Union {
                        left: left.data().to_owned(),
                        right: right.data().to_owned(),
                    }),
                ))
            }
        }
    }
}

impl<P, L, R, F, T> Series for Union<P, L, R, F>
where
    P: Copy + Ord + std::fmt::Debug,
    L: Series<Point = P>,
    R: Series<Point = P>,
    R::Value: Copy + std::fmt::Debug,
    L::Value: Copy + std::fmt::Debug,
    F: Fn(UnionResult<L::Value, R::Value>) -> T,
{
    type Point = P;

    type Value = T;
}

#[cfg(test)]
mod tests {
    use crate::union::Value;

    #[test]
    fn test_value_ord() {
        assert!(Value::Value(1) == Value::Value(1));
        assert!(Value::Value(2) > Value::Value(1));
        assert!(Value::Value(1) < Value::Infinite);
    }

    mod cursor {
        use crate::union::{Cursor, CursorIterator};

        fn test<T>(expected: Vec<Cursor<T>>, x: Vec<T>)
        where
            T: std::marker::Copy + std::fmt::Debug + PartialEq,
        {
            let actual = CursorIterator::new(x.into_iter()).collect::<Vec<_>>();
            assert_eq!(expected.as_slice(), actual.as_slice())
        }

        #[test]
        fn test_empty_cursor() {
            let x: Vec<i32> = vec![];
            test(vec![], x);
        }

        #[test]
        fn test_single_cursor() {
            test(vec![Cursor::Single(1)], vec![1]);
        }

        #[test]
        fn test_simple_pair_cursor() {
            test(
                vec![Cursor::Pair { fst: 1, snd: 2 }, Cursor::Single(2)],
                vec![1, 2],
            );
        }

        #[test]
        fn test_pair_cursor() {
            test(
                vec![
                    Cursor::Pair { fst: 1, snd: 2 },
                    Cursor::Pair { fst: 2, snd: 3 },
                    Cursor::Pair { fst: 3, snd: 4 },
                    Cursor::Pair { fst: 4, snd: 5 },
                    Cursor::Single(5),
                ],
                vec![1, 2, 3, 4, 5],
            );
        }
    }

    mod union {
        use crate::{
            of_iter,
            union::{Union, UnionResult},
            DataPoint,
        };

        type Expected = Vec<DataPoint<i32, (Option<i64>, Option<u32>)>>;

        fn test(
            expected: Expected,
            left: Vec<DataPoint<i32, i64>>,
            right: Vec<DataPoint<i32, u32>>,
        ) {
            test_ex(expected, left, right, true)
        }

        fn test_ex(
            expected: Expected,
            left: Vec<DataPoint<i32, i64>>,
            right: Vec<DataPoint<i32, u32>>,
            can_mirror: bool,
        ) {
            fn to_option<L, R>(x: UnionResult<L, R>) -> (Option<L>, Option<R>) {
                match x {
                    UnionResult::LeftOnly(left) => (Some(left), None),
                    UnionResult::RightOnly(right) => (None, Some(right)),
                    UnionResult::Union { left, right } => (Some(left), Some(right)),
                }
            }

            {
                let actual: Vec<_> =
                    Union::new(of_iter(left.clone()), of_iter(right.clone()), to_option).collect();
                assert_eq!(
                    expected.as_slice(),
                    actual.as_slice(),
                    "\n---- inputs\n1/\n left:{left:?}\nright:{right:?}\n"
                );
            }

            if can_mirror {
                let actual: Vec<_> =
                    Union::new(of_iter(right.clone()), of_iter(left.clone()), |x| {
                        let (l, r) = to_option(x);
                        (r, l)
                    })
                    .collect();
                assert_eq!(
                    expected.as_slice(),
                    actual.as_slice(),
                    "\n---- inputs\n2/\n left:{left:?}\nright:{right:?}\n"
                );
            }
        }

        #[test]
        fn test_empty_empty() {
            test(vec![], vec![], vec![]);
        }

        #[test]
        fn test_single_empty() {
            let left = vec![DataPoint::new(1, 100)];
            let expected = vec![DataPoint::new(1, (Some(100), None))];

            test(expected, left, vec![]);
        }

        #[test]
        fn test_singles_empty() {
            let left = vec![
                DataPoint::new(1, 100),
                DataPoint::new(3, 100),
                DataPoint::new(4, 100),
            ];
            let expected = vec![
                DataPoint::new(1, (Some(100), None)),
                DataPoint::new(3, (Some(100), None)),
                DataPoint::new(4, (Some(100), None)),
            ];

            test(expected, left, vec![]);
        }

        #[test]
        fn test_single_single() {
            let left = vec![DataPoint::new(2, 120)];
            let right = vec![DataPoint::new(1, 100)];
            let expected = vec![
                DataPoint::new(1, (None, Some(100))),
                DataPoint::new(2, (Some(120), Some(100))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_single_single_full_overlap() {
            let left = vec![DataPoint::new(1, 120)];
            let right = vec![DataPoint::new(1, 100)];
            let expected = vec![DataPoint::new(1, (Some(120), Some(100)))];

            test(expected, left, right);
        }

        #[test]
        fn test_single_pair() {
            let left = vec![DataPoint::new(10, 130)];
            let right = vec![DataPoint::new(1, 120), DataPoint::new(5, 200)];
            let expected = vec![
                DataPoint::new(1, (None, Some(120))),
                DataPoint::new(5, (None, Some(200))),
                DataPoint::new(10, (Some(130), Some(200))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_single_pair_2() {
            let left = vec![DataPoint::new(2, 120)];
            let right = vec![DataPoint::new(1, 100), DataPoint::new(3, 150)];
            let expected = vec![
                DataPoint::new(1, (None, Some(100))),
                DataPoint::new(2, (Some(120), Some(100))),
                DataPoint::new(3, (Some(120), Some(150))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_single_multiple_3() {
            let left = vec![DataPoint::new(1, 120)];
            let right = vec![DataPoint::new(2, 100), DataPoint::new(5, 150)];
            let expected = vec![
                DataPoint::new(1, (Some(120), None)),
                DataPoint::new(2, (Some(120), Some(100))),
                DataPoint::new(5, (Some(120), Some(150))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_partial_intersection() {
            let left = vec![
                DataPoint::new(1, 130),
                DataPoint::new(3, 120),
                DataPoint::new(10, 95),
            ];

            let right = vec![DataPoint::new(2, 120), DataPoint::new(10, 95)];

            let expected = vec![
                DataPoint::new(1, (Some(130), None)),
                DataPoint::new(2, (Some(130), Some(120))),
                DataPoint::new(3, (Some(120), Some(120))),
                DataPoint::new(10, (Some(95), Some(95))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_segmented_full_intersection() {
            let left = vec![
                DataPoint::new(1, 130),
                DataPoint::new(3, 120),
                DataPoint::new(10, 95),
            ];

            let right = vec![DataPoint::new(3, 120), DataPoint::new(10, 95)];

            let expected = vec![
                DataPoint::new(1, (Some(130), None)),
                DataPoint::new(3, (Some(120), Some(120))),
                DataPoint::new(10, (Some(95), Some(95))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_pair_pair() {
            let left = vec![DataPoint::new(10, 130), DataPoint::new(12, 140)];
            let right = vec![DataPoint::new(1, 120), DataPoint::new(5, 200)];
            let expected = vec![
                DataPoint::new(1, (None, Some(120))),
                DataPoint::new(5, (None, Some(200))),
                DataPoint::new(10, (Some(130), Some(200))),
                DataPoint::new(12, (Some(140), Some(200))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_multiple_first() {
            let left = vec![
                DataPoint::new(1, 130),
                DataPoint::new(2, 140),
                DataPoint::new(5, 150),
                DataPoint::new(20, 160),
            ];
            let right = vec![DataPoint::new(30, 120)];
            let expected = vec![
                DataPoint::new(1, (Some(130), None)),
                DataPoint::new(2, (Some(140), None)),
                DataPoint::new(5, (Some(150), None)),
                DataPoint::new(20, (Some(160), None)),
                DataPoint::new(30, (Some(160), Some(120))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_multiple_intersections() {
            let left = vec![DataPoint::new(1, 130), DataPoint::new(20, 160)];
            let right = vec![
                DataPoint::new(3, 120),
                DataPoint::new(5, 110),
                DataPoint::new(6, 100),
                DataPoint::new(10, 90),
                DataPoint::new(15, 190),
                DataPoint::new(19, 180),
            ];
            let expected = vec![
                DataPoint::new(1, (Some(130), None)),
                DataPoint::new(3, (Some(130), Some(120))),
                DataPoint::new(5, (Some(130), Some(110))),
                DataPoint::new(6, (Some(130), Some(100))),
                DataPoint::new(10, (Some(130), Some(90))),
                DataPoint::new(15, (Some(130), Some(190))),
                DataPoint::new(19, (Some(130), Some(180))),
                DataPoint::new(20, (Some(160), Some(180))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_multiple_intersections_overlap() {
            let left = vec![DataPoint::new(1, 130), DataPoint::new(20, 160)];
            let right = vec![
                DataPoint::new(3, 120),
                DataPoint::new(5, 110),
                DataPoint::new(6, 100),
                DataPoint::new(10, 90),
                DataPoint::new(15, 190),
                DataPoint::new(20, 180),
            ];
            let expected = vec![
                DataPoint::new(1, (Some(130), None)),
                DataPoint::new(3, (Some(130), Some(120))),
                DataPoint::new(5, (Some(130), Some(110))),
                DataPoint::new(6, (Some(130), Some(100))),
                DataPoint::new(10, (Some(130), Some(90))),
                DataPoint::new(15, (Some(130), Some(190))),
                DataPoint::new(20, (Some(160), Some(180))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_multiple_intersections_overlaps() {
            let left = vec![
                DataPoint::new(1, 130),
                DataPoint::new(3, 120),
                DataPoint::new(10, 95),
                DataPoint::new(20, 160),
            ];
            let right = vec![
                DataPoint::new(3, 105),
                DataPoint::new(5, 110),
                DataPoint::new(6, 100),
                DataPoint::new(10, 90),
                DataPoint::new(15, 190),
                DataPoint::new(20, 180),
            ];
            let expected = vec![
                DataPoint::new(1, (Some(130), None)),
                DataPoint::new(3, (Some(120), Some(105))),
                DataPoint::new(5, (Some(120), Some(110))),
                DataPoint::new(6, (Some(120), Some(100))),
                DataPoint::new(10, (Some(95), Some(90))),
                DataPoint::new(15, (Some(95), Some(190))),
                DataPoint::new(20, (Some(160), Some(180))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_multiple_no_intersection() {
            let left = vec![
                DataPoint::new(1, 130),
                DataPoint::new(3, 120),
                DataPoint::new(10, 95),
                DataPoint::new(20, 160),
            ];
            let right = vec![DataPoint::new(12, 105), DataPoint::new(15, 110)];
            let expected = vec![
                DataPoint::new(1, (Some(130), None)),
                DataPoint::new(3, (Some(120), None)),
                DataPoint::new(10, (Some(95), None)),
                DataPoint::new(12, (Some(95), Some(105))),
                DataPoint::new(15, (Some(95), Some(110))),
                DataPoint::new(20, (Some(160), Some(110))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_full_intersection() {
            let left = vec![
                DataPoint::new(1, 130),
                DataPoint::new(3, 120),
                DataPoint::new(10, 95),
                DataPoint::new(20, 160),
            ];

            let right = vec![
                DataPoint::new(1, 130),
                DataPoint::new(3, 120),
                DataPoint::new(10, 95),
                DataPoint::new(20, 160),
            ];

            let expected = vec![
                DataPoint::new(1, (Some(130), Some(130))),
                DataPoint::new(3, (Some(120), Some(120))),
                DataPoint::new(10, (Some(95), Some(95))),
                DataPoint::new(20, (Some(160), Some(160))),
            ];

            test(expected, left, right);
        }

        #[test]
        fn test_full_intersection2() {
            let left = vec![
                DataPoint::new(-15, 130),
                DataPoint::new(-1, 130),
                DataPoint::new(1, 130),
                DataPoint::new(3, 120),
                DataPoint::new(10, 95),
                DataPoint::new(20, 160),
            ];

            let right = vec![
                DataPoint::new(1, 130),
                DataPoint::new(3, 120),
                DataPoint::new(10, 95),
                DataPoint::new(20, 160),
            ];

            let expected = vec![
                DataPoint::new(-15, (Some(130), None)),
                DataPoint::new(-1, (Some(130), None)),
                DataPoint::new(1, (Some(130), Some(130))),
                DataPoint::new(3, (Some(120), Some(120))),
                DataPoint::new(10, (Some(95), Some(95))),
                DataPoint::new(20, (Some(160), Some(160))),
            ];

            test(expected, left, right);
        }
    }
}
