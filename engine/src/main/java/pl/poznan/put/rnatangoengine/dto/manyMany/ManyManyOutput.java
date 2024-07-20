package pl.poznan.put.rnatangoengine.dto.manyMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.Angle;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableManyManyOutput.class)
@JsonDeserialize(as = ImmutableManyManyOutput.class)
public interface ManyManyOutput {
  String resultRemovedAfter();

  String model();

  String chain();

  List<Angle> requestedAngles();

  List<String> structureModels();

  List<ManyManyOneInstance> oneManyResults();
}
