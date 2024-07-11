package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableStructureModelResponse.class)
@JsonDeserialize(as = ImmutableStructureModelResponse.class)
public interface StructureModelResponse {

  String fileId();

  String fileName();

  Optional<String> sequence();

  Optional<Selection> selection();

  Selection sourceSelection();
}
