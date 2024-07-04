package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableOneManyOutput.class)
@JsonDeserialize(as = ImmutableOneManyOutput.class)
public interface OneManyOutput {
  String model();

  String chain();

  List<TorsionAngleDifferences> differences();
}
