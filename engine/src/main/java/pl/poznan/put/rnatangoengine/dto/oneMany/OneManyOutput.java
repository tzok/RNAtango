package pl.poznan.put.rnatangoengine.dto.oneMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.Angle;
import pl.poznan.put.rnatangoengine.dto.TorsionAngleDifferences;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableOneManyOutput.class)
@JsonDeserialize(as = ImmutableOneManyOutput.class)
public interface OneManyOutput {

  String resultRemovedAfter();

  String model();

  String targetHashId();

  String targetFileName();

  String chain();

  Double lcsThreshold();

  List<Angle> requestedAngles();

  List<TorsionAngleDifferences> differences();
}
