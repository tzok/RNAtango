package pl.poznan.put.rnatangoengine.dto.manyMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;
import pl.poznan.put.rnatangoengine.dto.TorsionAngleDifferences;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableManyManyOneInstance.class)
@JsonDeserialize(as = ImmutableManyManyOneInstance.class)
public interface ManyManyOneInstance {

  String targetHashId();

  String targetFileName();

  Double lcsThreshold();

  List<TorsionAngleDifferences> differences();
}
