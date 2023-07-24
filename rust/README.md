# dataseries

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