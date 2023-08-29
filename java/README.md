# dataseries

[![License:MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![build](https://github.com/cboudereau/dataseries/workflows/build-rs/badge.svg?branch=main&event=push)](https://github.com/cboudereau/dataseries/actions/workflows/build-java.yml?query=event%3Apush+branch%3Amain)
[![codecov](https://codecov.io/gh/cboudereau/dataseries/branch/main/graph/badge.svg?token=UFSTKQG9FY&flag=java)](https://codecov.io/gh/cboudereau/dataseries)


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

#### (TODO) simple
A simple example of ```union``` between 2 timeseries

#### (TODO) intersection
An intersection implementation using the ```union``` function.

### (TODO) eventual consistency and conflict resolution
The ```crdt``` example provides an example of the conflict-free replicated data type resolution based on data-series ```union```.

The ```VersionedValue``` defines the version (here a timestamp) to solve the conflict by taking the maximum version. The maximum is defined through the ```Comparable``` interface and used inside the given function used by ```union```.

The below example uses TimestampMicros to version the data and solve conflict by taking the highest version of a value.
```java
// TODO
```