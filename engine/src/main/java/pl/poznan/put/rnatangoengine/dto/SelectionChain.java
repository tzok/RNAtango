package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableSelectionChain.class)
@JsonDeserialize(as = ImmutableSelectionChain.class)
public interface SelectionChain {
  String name();

  Optional<String> sequence();

  NucleotideRange nucleotideRange();
}
