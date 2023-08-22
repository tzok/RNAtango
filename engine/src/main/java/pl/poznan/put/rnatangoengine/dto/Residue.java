package pl.poznan.put.rnatangoengine.dto;

import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
public interface Residue {
  String name();

  int number();

  Optional<String> icode();
}
