package pl.poznan.put.rnatangoengine.dto;

public class StructureComparingResult {
  private String sequence;
  private Integer targetFromInclusiveRelative;
  private Integer targetToInclusiveRelative;
  private Integer modelFromInclusiveRelative;
  private Integer modelToInclusiveRelative;

  private StructureChainSequence bestMatchChainSequence;

  public StructureComparingResult() {
    sequence = "";
    targetToInclusiveRelative = 0;
    targetFromInclusiveRelative = 0;
    modelFromInclusiveRelative = 0;
    modelToInclusiveRelative = 0;
    bestMatchChainSequence = null;
  }

  public Integer getTargetFromInclusiveRelative() {
    return targetFromInclusiveRelative;
  }

  public Integer getModelFromInclusiveRelative() {
    return modelFromInclusiveRelative;
  }

  public Integer getTargetToInclusiveRelative() {
    return targetToInclusiveRelative;
  }

  public Integer getModelToInclusiveRelative() {
    return modelToInclusiveRelative;
  }

  public Integer getLength() {
    return this.targetToInclusiveRelative - this.targetFromInclusiveRelative + 1;
  }

  public String getSequence() {
    return sequence;
  }

  public StructureChainSequence getModel() {
    return bestMatchChainSequence;
  }

  public void setTargetFromInclusiveRelative(Integer targetFromInclusiveRelative) {
    this.targetFromInclusiveRelative = targetFromInclusiveRelative;
  }

  public void setModelFromInclusiveRelative(Integer modelFromInclusiveRelative) {
    this.modelFromInclusiveRelative = modelFromInclusiveRelative;
  }

  public void setTargetToInclusiveRelative(Integer targetToInclusiveRelative) {
    this.targetToInclusiveRelative = targetToInclusiveRelative;
  }

  public void setModelToInclusiveRelative(Integer modelToInclusiveRelative) {
    this.modelToInclusiveRelative = modelToInclusiveRelative;
  }

  public void setSequence(String sequence) {
    this.sequence = sequence;
  }

  public void setModel(StructureChainSequence chainSequence) {
    this.bestMatchChainSequence = chainSequence;
  }
}
