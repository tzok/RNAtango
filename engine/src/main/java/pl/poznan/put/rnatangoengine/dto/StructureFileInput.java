package pl.poznan.put.rnatangoengine.dto;

import org.immutables.value.Value;

@Value.Immutable
public interface StructureFileInput {
  File file();
}
