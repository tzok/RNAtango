package pl.poznan.put.rnatangoengine.dto.manyMany;

import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.Angle;

@Value.Immutable
public interface ManyManySubmitFormInput {
  List<String> files();

  List<Angle> angles();

  double threshold();
}
