use dataseries::{of_iter, DataPoint, Series, UnionResult};

fn map_any(x: UnionResult<i64, u32>) -> (Option<i64>, Option<u32>) {
    match x {
        UnionResult::LeftOnly(left) => (Some(left), None),
        UnionResult::RightOnly(right) => (None, Some(right)),
        UnionResult::Both { left, right } => (Some(left), Some(right)),
    }
}

#[test]
fn test_empty() {
    let x: dataseries::FromIteratorSeries<std::vec::IntoIter<DataPoint<i32, i64>>> =
        of_iter(vec![]);
    let y: dataseries::FromIteratorSeries<std::vec::IntoIter<DataPoint<i32, u32>>> =
        of_iter(vec![]);
    let u = x.union(y, |x| -> (Option<i64>, Option<u32>) { map_any(x) });
    let actual = u.collect::<Vec<_>>();
    type Expected = Vec<DataPoint<i32, (Option<i64>, Option<u32>)>>;
    let expected: Expected = vec![];

    assert_eq!(expected.as_slice(), actual.as_slice())
}

#[test]
fn test_right_empty() {
    let x = of_iter(vec![DataPoint::new(1, 2)]);
    let y = of_iter(vec![]);
    let u = x.union(y, map_any);
    let actual = u.collect::<Vec<_>>();
    let expected = vec![DataPoint::new(1, (Some(2), None))];

    assert_eq!(expected.as_slice(), actual.as_slice())
}

#[test]
fn test_left_empty() {
    let x = of_iter(vec![]);
    let y = of_iter(vec![DataPoint::new(1, 2)]);
    let actual = x.union(y, map_any).collect::<Vec<_>>();
    let expected = vec![DataPoint::new(1, (None, Some(2)))];

    assert_eq!(expected.as_slice(), actual.as_slice())
}
