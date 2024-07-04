package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableOneManySubmitFormInput.class)
@JsonDeserialize(as = ImmutableOneManySubmitFormInput.class)
public interface OneManySubmitFormInput {
  String taskHashId();

  List<Angle> angles();

  double threshold();
}
