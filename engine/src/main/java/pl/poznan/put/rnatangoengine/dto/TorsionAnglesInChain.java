package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableTorsionAnglesInChain.class)
@JsonDeserialize(as = ImmutableTorsionAnglesInChain.class)
public interface TorsionAnglesInChain {
  Chain chain();

  List<Residue> residues();
}
