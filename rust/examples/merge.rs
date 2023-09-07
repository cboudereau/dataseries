use dataseries::{DataPoint, Series};

fn contiguous() {
    let s = dataseries::of_iter(vec![DataPoint::new(1, 100), DataPoint::new(3, 100)]);

    let actual = s.merge().collect::<Vec<_>>();
    let expected = vec![DataPoint::new(1, 100)];

    assert_eq!(expected.as_slice(), actual.as_slice())
}

fn noncontiguous() {
    let s = dataseries::of_iter(vec![DataPoint::new(1, 100), DataPoint::new(3, 10)]);

    let actual = s.merge().collect::<Vec<_>>();
    let expected = vec![DataPoint::new(1, 100), DataPoint::new(3, 10)];

    assert_eq!(expected.as_slice(), actual.as_slice())
}

fn main() {
    contiguous();
    noncontiguous();
}
