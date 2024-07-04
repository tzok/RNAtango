package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableResidue.class)
@JsonDeserialize(as = ImmutableResidue.class)
public interface Residue {
  String name();

  int number();

  String dotBracketSymbol();

  Optional<String> icode();

  List<AngleValue> torsionAngles();
}
