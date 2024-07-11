package pl.poznan.put.rnatangoengine.dto.oneMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.Selection;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableOneManySetFormInput.class)
@JsonDeserialize(as = ImmutableOneManySetFormInput.class)
public interface OneManySetFormInput {
  String targetHashId();

  Selection selection();
}
