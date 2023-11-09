package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableAngleValue.class)
@JsonDeserialize(as = ImmutableAngleValue.class)
public interface AngleValue {
  Angle angle();

  Double value();
}
