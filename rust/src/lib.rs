//! dataseries
//! 
//! functions for dataseries :
//! - __union__ : combines 2 data-series to one.
//! 
//! examples :
//! - __simple__ : a basic usage of data-series union.
//! - __intersection__ : a simple intersection implementation based on union.
//! - __crdt__ : conflict-free replicated data type implementation of time-series updates to merge.
//! 
//! # Quick Start
//! ```rust
//! use dataseries::{DataPoint, Series};
//! 
//! fn test<T>(test_name: &str, expected: T, actual: T)
//! where
//!     T: PartialEq + std::fmt::Debug,
//! {
//!     if expected == actual {
//!         println!("expected: {expected:?}");
//!         println!("actual: {actual:?}");
//!         println!("{test_name} ok!");
//! 
//!         println!("---------------------------------")
//!     } else {
//!         panic!("{test_name} failed!\nexpected '{expected:?}'\n  actual '{actual:?}'")
//!     }
//! }
//! 
//! fn main() {
//!     let s1 = dataseries::of_iter(vec![DataPoint::new(3, 50)]);
//! 
//!     let s2 = dataseries::of_iter(vec![DataPoint::new(4, 100), DataPoint::new(7, 110)]);
//! 
//!     let actual = s1
//!         .union(s2, |x| match x {
//!             dataseries::UnionResult::LeftOnly(left) => (Some(left), None),
//!             dataseries::UnionResult::RightOnly(right) => (None, Some(right)),
//!             dataseries::UnionResult::Union { left, right } => (Some(left), Some(right)),
//!         })
//!         .collect::<Vec<_>>();
//! 
//!     let expected = vec![
//!         DataPoint::new(3, (Some(50), None)),
//!         DataPoint::new(4, (Some(50), Some(100))),
//!         DataPoint::new(7, (Some(50), Some(110))),
//!     ];
//! 
//!     test(
//!         "simple union example using option",
//!         expected.as_slice(),
//!         actual.as_slice(),
//!     );
//!     println!("done")
//! }
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
