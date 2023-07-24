mod datapoint;
mod merge;
mod union;
mod versioned_value;

pub use datapoint::DataPoint;
pub use merge::Merge;
pub use union::{Union, UnionResult};
pub use versioned_value::VersionedValue;

pub struct FromIteratorSeries<T> {
    iterator: T,
}

impl<IT, T> Iterator for FromIteratorSeries<IT>
where
    IT: Iterator<Item = T>,
{
    type Item = T;

    fn next(&mut self) -> Option<Self::Item> {
        self.iterator.next()
    }
}

impl<IT, Point, Value> Series for FromIteratorSeries<IT>
where
    IT: Iterator<Item = DataPoint<Point, Value>>,
{
    type Point = Point;

    type Value = Value;
}

pub trait Series: Iterator<Item = DataPoint<Self::Point, Self::Value>> + Sized {
    type Point;
    type Value;

    fn merge(self) -> Merge<Self> {
        Merge::new(self)
    }

    fn union<R, F, T>(self, other: R, f: F) -> Union<Self::Point, Self, R, F>
    where
        R: Series<Point = Self::Point>,
        F: Fn(UnionResult<Self::Value, R::Value>) -> T,
    {
        Union::new(self, other, f)
    }
}

//TODO : convert with From trait impl instead ?
pub fn of_iter<T, IT>(into_iter: T) -> FromIteratorSeries<IT>
where
    IT: Iterator,
    T: IntoIterator<IntoIter = IT>,
{
    let iterator = into_iter.into_iter();
    FromIteratorSeries { iterator }
}

impl<IT, P, V> From<IT> for FromIteratorSeries<IT>
where
    IT: Iterator<Item = DataPoint<P, V>>,
{
    fn from(iterator: IT) -> Self {
        FromIteratorSeries { iterator }
    }
}

#[cfg(test)]
mod tests {

    mod union {
        use crate::{of_iter, union::UnionResult, DataPoint, Series};

        fn map_any(x: UnionResult<i64, u32>) -> (Option<i64>, Option<u32>) {
            match x {
                UnionResult::LeftOnly(left) => (Some(left), None),
                UnionResult::RightOnly(right) => (None, Some(right)),
                UnionResult::Union { left, right } => (Some(left), Some(right)),
            }
        }

        #[test]
        fn test_empty() {
            let x: crate::FromIteratorSeries<std::vec::IntoIter<DataPoint<i32, i64>>> =
                of_iter(vec![]);
            let y: crate::FromIteratorSeries<std::vec::IntoIter<DataPoint<i32, u32>>> =
                of_iter(vec![]);
            let u = x.union(y, |x| -> (Option<i64>, Option<u32>) { map_any(x) });
            let actual = u.collect::<Vec<_>>();
            type Expected = Vec<DataPoint<i32, (Option<i64>, Option<u32>)>>;
            let expected: Expected = vec![];

            assert_eq!(expected.as_slice(), actual.as_slice())
        }

        #[test]
        fn test_right_empty() {
            let x = of_iter(vec![DataPoint::new(1, 2)]);
            let y = of_iter(vec![]);
            let u = x.union(y, map_any);
            let actual = u.collect::<Vec<_>>();
            let expected = vec![DataPoint::new(1, (Some(2), None))];

            assert_eq!(expected.as_slice(), actual.as_slice())
        }

        #[test]
        fn test_left_empty() {
            let x = of_iter(vec![]);
            let y = of_iter(vec![DataPoint::new(1, 2)]);
            let actual = x.union(y, map_any).collect::<Vec<_>>();
            let expected = vec![DataPoint::new(1, (None, Some(2)))];

            assert_eq!(expected.as_slice(), actual.as_slice())
        }
    }
}
