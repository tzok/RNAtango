package pl.poznan.put.rnatangoengine.dto.manyMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.Angle;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableManyManySubmitFormInput.class)
@JsonDeserialize(as = ImmutableManyManySubmitFormInput.class)
public interface ManyManySubmitFormInput {
  String taskHashId();

  String chain();

  List<Angle> angles();

  double threshold();
}
