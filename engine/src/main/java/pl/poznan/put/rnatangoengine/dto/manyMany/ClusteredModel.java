package pl.poznan.put.rnatangoengine.dto.manyMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableClusteredModel.class)
@JsonDeserialize(as = ImmutableClusteredModel.class)
public interface ClusteredModel {
  double x();

  double y();

  String name();

  int clusterNumber();
}
