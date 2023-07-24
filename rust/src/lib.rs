mod datapoint;
mod merge;
mod union;

pub use datapoint::DataPoint;
pub use merge::Merge;
pub use union::{Union, UnionResult};

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

pub fn of_iter<T, IT>(into_iter: T) -> FromIteratorSeries<IT>
where
    IT: Iterator,
    T: IntoIterator<IntoIter = IT>,
{
    let iterator = into_iter.into_iter();
    FromIteratorSeries { iterator }
}
