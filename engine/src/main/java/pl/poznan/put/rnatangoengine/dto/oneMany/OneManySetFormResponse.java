package pl.poznan.put.rnatangoengine.dto.oneMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.StructureModelResponse;
import pl.poznan.put.rnatangoengine.dto.StructureTargetResponse;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableOneManySetFormResponse.class)
@JsonDeserialize(as = ImmutableOneManySetFormResponse.class)
public interface OneManySetFormResponse {
  StructureTargetResponse target();

  List<StructureModelResponse> models();
}
