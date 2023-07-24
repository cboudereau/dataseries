use dataseries::{DataPoint, Series};

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
    let s1 = dataseries::of_iter(vec![DataPoint::new(3, 50)]);

    let s2 = dataseries::of_iter(vec![DataPoint::new(4, 100), DataPoint::new(7, 110)]);

    let actual = s1
        .union(s2, |x| match x {
            dataseries::UnionResult::LeftOnly(left) => (Some(left), None),
            dataseries::UnionResult::RightOnly(right) => (None, Some(right)),
            dataseries::UnionResult::Union { left, right } => (Some(left), Some(right)),
        })
        .collect::<Vec<_>>();

    let expected = vec![
        DataPoint::new(3, (Some(50), None)),
        DataPoint::new(4, (Some(50), Some(100))),
        DataPoint::new(7, (Some(50), Some(110))),
    ];

    test(
        "simple union example using option",
        expected.as_slice(),
        actual.as_slice(),
    );
    println!("done")
}
