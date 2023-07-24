use std::time::Duration;

use criterion::{black_box, criterion_group, criterion_main, Criterion};
use dataseries::{
    {DataPoint, Series, UnionResult},
    VersionedValue,
};
use rand::Rng;

fn to_option<L, R>(x: UnionResult<L, R>) -> (Option<L>, Option<R>) {
    match x {
        UnionResult::LeftOnly(left) => (Some(left), None),
        UnionResult::RightOnly(right) => (None, Some(right)),
        UnionResult::Union { left, right } => (Some(left), Some(right)),
    }
}

fn union_simple_benchmark(c: &mut Criterion) {
    c.bench_function("simple union + merge", |b| {
        b.iter(|| {
            let x = dataseries::of_iter(vec![
                DataPoint::new(i32::MIN, VersionedValue::new(i32::MIN, None)),
                DataPoint::new(1, VersionedValue::new(1, Some(100))),
                DataPoint::new(10, VersionedValue::new(1, None)),
                DataPoint::new(15, VersionedValue::new(1, Some(150))),
                DataPoint::new(300, VersionedValue::new(1, None)),
                DataPoint::new(315, VersionedValue::new(1, Some(150))),
                DataPoint::new(316, VersionedValue::new(1, None)),
            ]);

            let y = dataseries::of_iter(vec![
                DataPoint::new(i32::MIN, VersionedValue::new(i32::MIN, None)),
                DataPoint::new(2, VersionedValue::new(2, Some(100))),
                DataPoint::new(3, VersionedValue::new(2, None)),
                DataPoint::new(3, VersionedValue::new(2, Some(150))),
                DataPoint::new(4, VersionedValue::new(2, None)),
                DataPoint::new(10, VersionedValue::new(2, Some(150))),
                DataPoint::new(50, VersionedValue::new(2, None)),
                DataPoint::new(60, VersionedValue::new(2, Some(150))),
                DataPoint::new(80, VersionedValue::new(2, None)),
                DataPoint::new(100, VersionedValue::new(2, Some(150))),
                DataPoint::new(1200, VersionedValue::new(2, None)),
            ]);

            black_box(x.union(y, to_option).merge()).for_each(|_| ());
        })
    });
}



fn union_complex_benchmark(c: &mut Criterion) {
    c.bench_function("complex union + merge", |b| {
        b.iter(|| {
            let mut rng = rand::thread_rng();
            let x = {
                let r = 1..1_000_000;
                dataseries::of_iter(r.flat_map(|x| {
                    vec![
                        DataPoint::new(x, VersionedValue::new(1, Some(rng.gen_range(10..100)))),
                        DataPoint::new(x + rng.gen_range(0..100), VersionedValue::new(1, None)),
                    ]
                }))
            };
            let mut rng = rand::thread_rng();
            let y = {
                let r = 1..100_000;
                dataseries::of_iter(r.flat_map(|x| {
                    vec![
                        DataPoint::new(x, VersionedValue::new(2, Some(rng.gen_range(50..60)))),
                        DataPoint::new(x + rng.gen_range(0..1000), VersionedValue::new(2, None)),
                    ]
                }))
            };

            black_box(x.union(y, to_option).merge()).for_each(|_| ());
        })
    });
}


criterion_group! {
    name = benches;
    config = Criterion::default().measurement_time(Duration::from_secs(40)).sample_size(200);
    targets =
        union_simple_benchmark,
        union_complex_benchmark,
}
criterion_main!(benches);
