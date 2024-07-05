package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableLCSResult.class)
@JsonDeserialize(as = ImmutableLCSResult.class)
public interface LCSResult {
  NucleotideRange targetNucleotideRange();

  NucleotideRange modelNucleotideRange();

  double validResidues();

  double coveragePercent();

  double fragmentMCQ();
}
