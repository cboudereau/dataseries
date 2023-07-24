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

## eventual consistency and conflict resolution for data-series
The ```crdt``` example provides an example of the [conflict-free replicated data type](https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type) resolution based on data-series ```union``` function and ``VersionedValue`` type to solve conflict with a timestamp (any variable supporting [partially ordered set](https://en.wikipedia.org/wiki/Partially_ordered_set)) for rust and java.

## trade-offs

### interval representation
An interval can be defined by using 2 points (from and to) with an associated value but it can be difficult to index those 2 points in nosql databases (Global secondary index) or simply using a TSDB (timeseries database).

Another approach consists of defining an intermediate model, a data-series with only one point and one value so that the datapoint feet really well with TSDB and is algorithm friendly. 

It becomes also easy to avoid unwanted states; an interval can be defined with 2 points and the last point can be before the first one which is a bug in the domain. You can also define a point and a non negative offset which can work but requires more code.

For database support, interval reading requires 2 reads to compute the interval but the extra read can be hidden in easily in an Iterator.

## implementation

rust and java implementation are provided in respective directories.