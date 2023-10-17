package pl.poznan.put.rnatangoengine.dto;

import org.immutables.value.Value;

@Value.Immutable
public interface AngleValue {
  Angle angle();

  Double value();
}
