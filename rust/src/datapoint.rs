#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct DataPoint<P, T> {
    point: P,
    data: T,
}

impl<P, T> DataPoint<P, T> {
    pub fn new(point: P, data: T) -> Self {
        Self { point, data }
    }

    pub fn point(&self) -> &P {
        &self.point
    }

    pub fn data(&self) -> &T {
        &self.data
    }
}
