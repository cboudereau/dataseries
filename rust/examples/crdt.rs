//! Eventual Consistency and Conflict Resolution
//!
//! [`VersionedValue`] version field is used to identify the version of a value.
//! The relational order is used to take the max version on conflict when [`dataseries::UnionResult::Union`] occurs (this is why it is important to keep the version field to be the first defined field)
//! When there is no conflict ([`dataseries::UnionResult::LeftOnly`] or [`dataseries::UnionResult::RightOnly`]), only the available [`VersionedValue`] is used
//!
use dataseries::{DataPoint, Series};
#[derive(Debug, Clone, Copy, PartialEq, Eq, PartialOrd, Ord)]
pub struct VersionedValue<V, T> {
    /// The first field should be the version field to make [`Ord`] macro compliant
    /// with the fact that only version is important to solve conflict and has to be checked first with the highest weight
    version: V,
    value: T,
}

impl<V, T> VersionedValue<V, T>
where
    V: PartialOrd + Copy,
    T: Copy,
{
    pub fn new(version: V, value: T) -> Self {
        Self { version, value }
    }

    pub fn version(&self) -> &V {
        &self.version
    }

    pub fn value(&self) -> &T {
        &self.value
    }
}

#[derive(Debug, Clone, Copy, Eq, Ord, PartialEq, PartialOrd)]
pub struct TimestampMicros(i64);

impl TimestampMicros {
    pub const MIN: TimestampMicros = TimestampMicros(i64::MIN);
    pub const MAX: TimestampMicros = TimestampMicros(i64::MAX);
    pub fn new(timestamp_micros: i64) -> Self {
        TimestampMicros(timestamp_micros)
    }
}

impl From<TimestampMicros> for i64 {
    fn from(value: TimestampMicros) -> Self {
        value.0
    }
}

#[derive(Debug, Copy, Clone, PartialEq, Eq, PartialOrd, Ord)]
struct Date {
    pub year: i32,
    pub month: u32,
    pub day: u32,
}

fn date(year: i32, month: u32, day: u32) -> Date {
    Date { year, month, day }
}

fn datapoint<T>(
    timestamp_micros: TimestampMicros,
    date: Date,
    data: T,
) -> DataPoint<Date, Option<VersionedValue<TimestampMicros, T>>>
where
    T: std::marker::Copy,
{
    DataPoint::new(date, Some(VersionedValue::new(timestamp_micros, data)))
}

/// Interval can be encoded by using 2 Datapoints with a [`None`] last datapoint value to mark the end of each interval
fn end<T>(date: Date) -> DataPoint<Date, Option<VersionedValue<TimestampMicros, T>>> {
    DataPoint::new(date, None)
}

fn test<T>(test_name: &str, expected: T, actual: T)
where
    T: PartialEq + std::fmt::Debug,
{
    if expected == actual {
        println!("expected: {expected:?}");
        println!("actual: {actual:?}");
        println!("{test_name} ok!");

        println!("---------------------------------")
    } else {
        panic!("{test_name} failed!\nexpected '{expected:?}'\n  actual '{actual:?}'")
    }
}

fn test_resolve_conflicts() {
    let s1 = dataseries::of_iter(vec![
        datapoint(TimestampMicros::new(1), date(2023, 1, 3), 50),
        end(date(2023, 1, 10)),
    ]);

    let s2 = dataseries::of_iter(vec![
        datapoint(TimestampMicros::new(2), date(2023, 1, 4), 100),
        end(date(2023, 1, 5)),
        datapoint(TimestampMicros::new(2), date(2023, 1, 7), 110),
        end(date(2023, 1, 9)),
    ]);

    // Solves conflict by taking always the maximum version
    let actual = resolve_conflicts(s1, s2);

    let expected = vec![
        datapoint(TimestampMicros::new(1), date(2023, 1, 3), 50),
        datapoint(TimestampMicros::new(2), date(2023, 1, 4), 100),
        datapoint(TimestampMicros::new(1), date(2023, 1, 5), 50),
        datapoint(TimestampMicros::new(2), date(2023, 1, 7), 110),
        datapoint(TimestampMicros::new(1), date(2023, 1, 9), 50),
        end(date(2023, 1, 10)),
    ];

    test(
        "conflict free replicated data type merge test",
        expected.as_slice(),
        actual.as_slice(),
    );
}

fn test_no_conflict() {
    let s1 = dataseries::of_iter(vec![
        datapoint(TimestampMicros::new(1), date(2023, 1, 3), 50),
        end(date(2023, 1, 10)),
    ]);

    let s2 = dataseries::of_iter(vec![
        datapoint(TimestampMicros::new(2), date(2023, 1, 15), 100),
        end(date(2023, 1, 20)),
    ]);

    // Solves conflict by taking always the maximum version
    let actual = resolve_conflicts(s1, s2);

    let expected = vec![
        datapoint(TimestampMicros::new(1), date(2023, 1, 3), 50),
        end(date(2023, 1, 10)),
        datapoint(TimestampMicros::new(2), date(2023, 1, 15), 100),
        end(date(2023, 1, 20)),
    ];

    test(
        "no conflict to resolve test",
        expected.as_slice(),
        actual.as_slice(),
    );
}

fn resolve_conflicts(
    s1: dataseries::FromIteratorSeries<
        std::vec::IntoIter<DataPoint<Date, Option<VersionedValue<TimestampMicros, i32>>>>,
    >,
    s2: dataseries::FromIteratorSeries<
        std::vec::IntoIter<DataPoint<Date, Option<VersionedValue<TimestampMicros, i32>>>>,
    >,
) -> Vec<DataPoint<Date, Option<VersionedValue<TimestampMicros, i32>>>> {
    let actual = s1
        .union(s2, |x| match x {
            dataseries::UnionResult::LeftOnly(x) | dataseries::UnionResult::RightOnly(x) => x,
            dataseries::UnionResult::Both { left, right } => std::cmp::max(left, right),
        })
        .collect::<Vec<_>>();
    actual
}

fn main() {
    test_resolve_conflicts();
    test_no_conflict();
    println!("done")
}

#[cfg(test)]
mod test {
    use crate::VersionedValue;

    #[test]
    fn test_version_ord_impl() {
        assert!(VersionedValue::new(2, 1) > VersionedValue::new(1, 10));
        assert!(VersionedValue::new(2, 2) > VersionedValue::new(2, 1));
    }
}
