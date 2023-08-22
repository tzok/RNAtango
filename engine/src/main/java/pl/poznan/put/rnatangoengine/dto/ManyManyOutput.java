package pl.poznan.put.rnatangoengine.dto;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface ManyManyOutput {
  List<String> names();

  Matrix matrixMCQ();

  Matrix matrixLCS();
}
