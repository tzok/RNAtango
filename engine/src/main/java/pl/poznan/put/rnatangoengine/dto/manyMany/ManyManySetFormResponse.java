package pl.poznan.put.rnatangoengine.dto.manyMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.StructureModelResponse;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableManyManySetFormResponse.class)
@JsonDeserialize(as = ImmutableManyManySetFormResponse.class)
public interface ManyManySetFormResponse {

  String taskHashId();

  List<StructureModelResponse> models();

  List<LongestChainSequence> sequences();
}
