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
            dataseries::UnionResult::LeftOnly(_) | dataseries::UnionResult::RightOnly(_) => None,
            dataseries::UnionResult::Union { left, right } => Some((left, right)),
        })
        .filter_map(|x| {
            x.data()
                .map(|d| DataPoint::new(x.point().to_owned(), d.to_owned()))
        })
        .collect::<Vec<_>>();

    let expected = vec![DataPoint::new(4, (50, 100)), DataPoint::new(7, (50, 110))];

    test(
        "simple union example using option",
        expected.as_slice(),
        actual.as_slice(),
    );
    println!("done")
}
