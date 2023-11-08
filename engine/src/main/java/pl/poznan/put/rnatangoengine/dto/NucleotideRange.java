package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableNucleotideRange.class)
@JsonDeserialize(as = ImmutableNucleotideRange.class)
public interface NucleotideRange {
  int fromInclusive();

  int toInclusive();
}
