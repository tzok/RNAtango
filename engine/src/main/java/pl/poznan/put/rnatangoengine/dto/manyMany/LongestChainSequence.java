package pl.poznan.put.rnatangoengine.dto.manyMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableLongestChainSequence.class)
@JsonDeserialize(as = ImmutableLongestChainSequence.class)
public interface LongestChainSequence {
  String name();

  String sequence();
}
