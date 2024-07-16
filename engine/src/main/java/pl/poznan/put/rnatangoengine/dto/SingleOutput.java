package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import javax.annotation.Nullable;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableSingleOutput.class)
@JsonDeserialize(as = ImmutableSingleOutput.class)
public interface SingleOutput {
  List<TorsionAnglesInChain> torsionAngles();

  Boolean containDiscontinuousSequences();

  String resultRemovedAfter();

  @Nullable String structureName();

  @Nullable String structureMolecule();

  @Nullable String structureTitle();
}
