package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableStructureTargetResponse.class)
@JsonDeserialize(as = ImmutableStructureTargetResponse.class)
public interface StructureTargetResponse {
  String sequence();

  Selection sourceSelection();

  Selection selection();
}
