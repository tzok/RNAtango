package pl.poznan.put.rnatangoengine.dto;

import java.util.List;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface Selection {
  String modelName();

  Optional<List<SelectionChain>> chains();
}
