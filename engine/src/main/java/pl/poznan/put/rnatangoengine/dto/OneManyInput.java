package pl.poznan.put.rnatangoengine.dto;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface OneManyInput {
  String reference();

  List<String> models();

  List<Angle> angles();

  double threshold();
}
