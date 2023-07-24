use dataseries::{DataPoint, Series};

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct VersionedValue<V, T> {
    version: V,
    value: T,
}

impl<V, T> PartialOrd for VersionedValue<V, T>
where
    V: PartialOrd,
    T: PartialOrd,
{
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        match self.version.partial_cmp(&other.version) {
            Some(core::cmp::Ordering::Equal) => self.value.partial_cmp(&other.value),
            ord => ord,
        }
    }
}

impl<V, T> Ord for VersionedValue<V, T>
where
    V: Ord,
    T: Ord,
{
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        match self.version.cmp(&other.version) {
            core::cmp::Ordering::Equal => self.value.cmp(&other.value),
            ord => ord,
        }
    }
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

fn main() {
    let s1 = vec![
        datapoint(TimestampMicros::new(1), date(2023, 1, 3), 50),
        end(date(2023, 1, 10)),
    ];

    let s2 = vec![
        datapoint(TimestampMicros::new(2), date(2023, 1, 4), 100),
        end(date(2023, 1, 5)),
        datapoint(TimestampMicros::new(2), date(2023, 1, 7), 110),
        end(date(2023, 1, 9)),
    ];

    let s1 = dataseries::of_iter(s1);
    let s2 = dataseries::of_iter(s2);

    let actual = s1
        .union(s2, |x| match x {
            dataseries::UnionResult::LeftOnly(x) | dataseries::UnionResult::RightOnly(x) => x,
            dataseries::UnionResult::Union { left, right } => std::cmp::max(left, right),
        })
        .collect::<Vec<_>>();

    let expected = vec![
        datapoint(TimestampMicros::new(1), date(2023, 1, 3), 50),
        datapoint(TimestampMicros::new(2), date(2023, 1, 4), 100),
        datapoint(TimestampMicros::new(1), date(2023, 1, 5), 50),
        datapoint(TimestampMicros::new(2), date(2023, 1, 7), 110),
        datapoint(TimestampMicros::new(1), date(2023, 1, 9), 50),
        end(date(2023, 1, 10)),
    ];

    test("conflict free replicated data type merge test", expected.as_slice(), actual.as_slice());
    println!("done")
}
