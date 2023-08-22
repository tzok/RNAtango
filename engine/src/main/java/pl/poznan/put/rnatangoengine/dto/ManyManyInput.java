package pl.poznan.put.rnatangoengine.dto;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface ManyManyInput {
  List<String> files();

  List<Angle> angles();

  double threshold();
}
