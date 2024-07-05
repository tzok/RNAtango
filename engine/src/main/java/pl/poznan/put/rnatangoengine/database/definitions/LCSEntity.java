package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import pl.poznan.put.rnatangoengine.dto.ImmutableLCSResult;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.LCSResult;

@Entity
@Table(name = "LCS")
public class LCSEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  int targetFromInclusive;
  int targetToInclusive;
  int modelFromInclusive;
  int modelToInclusive;

  double mcqValue;
  double coveragePercent;
  double validResidues;

  public LCSEntity() {}

  public void setTargetRange(int from, int to) {
    this.targetFromInclusive = from;
    this.targetToInclusive = to;
  }

  public void setModelRange(int from, int to) {
    this.modelFromInclusive = from;
    this.modelToInclusive = to;
  }

  public void setCoveragePercent(double percent) {
    this.coveragePercent = percent;
  }

  public void setValidResidues(double validResiduesCount) {
    this.validResidues = validResiduesCount;
  }

  public void setMcqValue(double mcq) {
    this.mcqValue = mcq;
  }

  public LCSResult getConvertedToLCSImmutable() {
    return ImmutableLCSResult.builder()
        .coveragePercent(coveragePercent)
        .validResidues(validResidues)
        .modelNucleotideRange(
            ImmutableNucleotideRange.builder()
                .fromInclusive(modelFromInclusive)
                .toInclusive(modelToInclusive)
                .build())
        .targetNucleotideRange(
            ImmutableNucleotideRange.builder()
                .fromInclusive(targetFromInclusive)
                .toInclusive(targetToInclusive)
                .build())
        .fragmentMCQ(this.mcqValue)
        .build();
  }
}
