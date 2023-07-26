# dataseries

[![License:MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![build](https://github.com/cboudereau/dataseries/workflows/build-rs/badge.svg?branch=main&event=push)](https://github.com/cboudereau/dataseries/actions/workflows/build-rs.yml?query=event%3Apush+branch%3Amain)
[![codecov](https://codecov.io/gh/cboudereau/dataseries/branch/main/graph/badge.svg?token=UFSTKQG9FY&flag=rust)](https://codecov.io/gh/cboudereau/dataseries)
[![docs.rs](https://docs.rs/dataseries/badge.svg)](https://docs.rs/dataseries)
[![crates.io](https://img.shields.io/crates/v/dataseries.svg)](https://crates.io/crates/dataseries)
[![crates.io (recent)](https://img.shields.io/crates/dr/dataseries)](https://crates.io/crates/dataseries)

data-series functions support for data-series and time-series.

## functions

### union

Continuous time series union between 2 series. Left and right data can be absent (left and right only cases).

```
          1     3     10                 20
    Left: |-----|-----|------------------|-
          130   120   95                 160
                           12     15
   Right:                  |------|--------
                           105    110
          1     3     10   12     15     20
Expected: |-----|-----|----|------|------|-
          130,∅ 120,∅ 95,∅ 95,105 95,110 160,110

```

### examples

#### simple
A simple example of ```union``` between 2 timeseries

#### intersection
An intersection implementation using the ```union``` function.

### eventual consistency and conflict resolution
The ```crdt``` example provides an example of the conflict-free replicated data type resolution based on data-series ```union```.

The ```VersionedValue``` defines the version (here a timestamp) to solve the conflict by taking the maximum version. The maximum is defined through the trait ```Ord``` and used inside the given function used by ```union```.

The below example uses TimestampMicros to version the data and solve conflict by taking the highest version of a value.
```rust
fn datapoint<T>(timestamp_micros: TimestampMicros, date: Date, data: T) -> DataPoint<Date, Option<VersionedValue<TimestampMicros, T>>>
where
    T: std::marker::Copy,
{
    DataPoint::new(date, Some(VersionedValue::new(timestamp_micros, data)))
}


/// Interval can be encoded by using 2 Datapoints with a [`None`] last datapoint value to mark the end of each interval
fn end<T>(date: Date) -> DataPoint<Date, Option<VersionedValue<TimestampMicros, T>>> {
    DataPoint::new(date, None)
}

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
```