package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableChain.class)
@JsonDeserialize(as = ImmutableChain.class)
public interface Chain {
  String name();

  String sequence();

  List<Integer> residuesWithoutAtoms();
}
