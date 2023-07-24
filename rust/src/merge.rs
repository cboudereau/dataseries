use super::Series;

pub struct Merge<TS>
where
    TS: Series,
{
    iterator: TS,
    current: Option<TS::Item>,
    is_done: bool,
}

impl<TS> Merge<TS>
where
    TS: Series,
{
    pub fn new(iterator: TS) -> Self {
        Self {
            iterator,
            current: None,
            is_done: false,
        }
    }
}

impl<TS> Iterator for Merge<TS>
where
    TS: Series,
    TS::Value: Copy + PartialOrd,
    TS::Point: Copy + PartialOrd,
{
    type Item = TS::Item;

    fn next(&mut self) -> Option<Self::Item> {
        loop {
            if self.is_done {
                return None;
            }

            match (self.current, self.iterator.next()) {
                (None, None) => return None,
                (None, Some(c)) => self.current = Some(c),
                (Some(p), None) => {
                    self.is_done = true;
                    return Some(p);
                }
                (Some(p), Some(c)) => {
                    if p.data() == c.data() {
                        self.current = Some(if p.point() < c.point() { p } else { c });
                    } else {
                        self.current = Some(c);
                        return Some(p);
                    }
                }
            }
        }
    }
}

impl<TS> Series for Merge<TS>
where
    TS: Series,
    TS::Point: Copy + PartialOrd,
    TS::Value: Copy + PartialOrd,
{
    type Point = TS::Point;

    type Value = TS::Value;
}

#[cfg(test)]
mod tests {
    use crate::{
        DataPoint,
        VersionedValue, Series,
    };

    #[test]
    fn test_merge_with_same_version_and_value() {
        let x = vec![
            DataPoint::new(1, VersionedValue::new(1, Some(10))),
            DataPoint::new(5, VersionedValue::new(1, Some(10))),
            DataPoint::new(10, VersionedValue::new(1, None)),
        ];

        let expected = vec![
            DataPoint::new(1, VersionedValue::new(1, Some(10))),
            DataPoint::new(10, VersionedValue::new(1, None)),
        ];

        let actual = crate::of_iter(x).merge().collect::<Vec<_>>();
        assert_eq!(expected.as_slice(), actual.as_slice());
    }

    #[test]
    fn test_merge_with_different_version_same_value() {
        let x = vec![
            DataPoint::new(1, VersionedValue::new(1, Some(10))),
            DataPoint::new(5, VersionedValue::new(2, Some(10))),
            DataPoint::new(10, VersionedValue::new(1, None)),
        ];

        let expected = vec![
            DataPoint::new(1, VersionedValue::new(1, Some(10))),
            DataPoint::new(5, VersionedValue::new(2, Some(10))),
            DataPoint::new(10, VersionedValue::new(1, None)),
        ];

        let actual = crate::of_iter(x).merge().collect::<Vec<_>>();
        assert_eq!(expected.as_slice(), actual.as_slice());
    }

    #[test]
    fn test_merge_with_same_version_different_value() {
        let x = vec![
            DataPoint::new(1, VersionedValue::new(1, Some(10))),
            DataPoint::new(5, VersionedValue::new(1, Some(100))),
            DataPoint::new(10, VersionedValue::new(1, None)),
        ];

        let expected = vec![
            DataPoint::new(1, VersionedValue::new(1, Some(10))),
            DataPoint::new(5, VersionedValue::new(1, Some(100))),
            DataPoint::new(10, VersionedValue::new(1, None)),
        ];

        let actual = crate::of_iter(x).merge().collect::<Vec<_>>();
        assert_eq!(expected.as_slice(), actual.as_slice());
    }
}
