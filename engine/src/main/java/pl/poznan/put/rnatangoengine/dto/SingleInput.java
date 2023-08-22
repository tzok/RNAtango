package pl.poznan.put.rnatangoengine.dto;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface SingleInput {
  String fileId();

  List<Selection> selections();
}
