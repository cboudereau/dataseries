#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct VersionedValue<V, T> {
    version: V,
    value: T,
}

impl<V, T> PartialOrd for VersionedValue<V, T>
where
    V: PartialOrd,
    T: PartialOrd,
{
    fn partial_cmp(&self, other: &Self) -> Option<std::cmp::Ordering> {
        match self.version.partial_cmp(&other.version) {
            Some(core::cmp::Ordering::Equal) => self.value.partial_cmp(&other.value),
            ord => ord,
        }
    }
}

// TODO : unit test + does it need Ord ??
impl<V, T> Ord for VersionedValue<V, T>
where
    V: Ord,
    T: Ord,
{
    fn cmp(&self, other: &Self) -> std::cmp::Ordering {
        match self.version.cmp(&other.version) {
            core::cmp::Ordering::Equal => self.value.cmp(&other.value),
            ord => ord,
        }
    }
}

impl<V, T> VersionedValue<V, T>
where
    V: PartialOrd + Copy,
    T: Copy,
{
    //TODO : How about zero with MinValue trait for version and TS to support contiguous feature of a timeseries (and about a true union ?)?
    // TODO : how to check invariant (monotonicity ?... The fact that the point is always going in the future and and never come back)
    pub fn new(version: V, value: T) -> Self {
        Self { version, value }
    }

    pub fn version(&self) -> &V {
        &self.version
    }

    pub fn value(&self) -> &T {
        &self.value
    }
}
