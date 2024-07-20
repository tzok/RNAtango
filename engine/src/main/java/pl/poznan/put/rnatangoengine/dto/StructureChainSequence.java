package pl.poznan.put.rnatangoengine.dto;

public class StructureChainSequence {
  String name;
  String sequence;
  Integer fromInclusive;
  Integer toInclusive;

  public StructureChainSequence(
      String name, String sequence, Integer fromInclusive, Integer toInclusive) {
    this.name = name;
    this.sequence = sequence;
    this.fromInclusive = fromInclusive;
    this.toInclusive = toInclusive;
  }

  public Integer getFrom() {
    return fromInclusive;
  }

  public Integer getTo() {
    return toInclusive;
  }

  public Integer getLength() {
    return this.toInclusive - this.fromInclusive;
  }

  public String getSequence() {
    return sequence;
  }
}
