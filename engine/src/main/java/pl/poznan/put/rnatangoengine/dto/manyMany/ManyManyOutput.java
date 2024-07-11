package pl.poznan.put.rnatangoengine.dto.manyMany;

import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.Matrix;

@Value.Immutable
public interface ManyManyOutput {
  List<String> names();

  Matrix matrixMCQ();

  Matrix matrixLCS();
}
