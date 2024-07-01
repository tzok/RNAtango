package pl.poznan.put.rnatangoengine.dto;

public class IndexPair {
  public int fromInclusive;
  public int toInclusive;

  public IndexPair(int fromInclusive, int toInclusive) {
    this.fromInclusive = fromInclusive;
    this.toInclusive = toInclusive;
  }
}
