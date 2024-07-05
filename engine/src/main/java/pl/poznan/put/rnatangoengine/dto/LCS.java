package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableLCS.class)
@JsonDeserialize(as = ImmutableLCS.class)
public interface LCS {
  NucleotideRange targetNucleotideRange();

  NucleotideRange modelNucleotideRange();

  double validResidues();

  double coveragePercent();
}
