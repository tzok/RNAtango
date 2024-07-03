package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableStructureModelResponse.class)
@JsonDeserialize(as = ImmutableStructureModelResponse.class)
public interface StructureModelResponse {

  String fileId();

  String sequence();

  Selection selection();

  Selection sourceSelection();
}
