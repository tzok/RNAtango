package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableStatusResponse.class)
@JsonDeserialize(as = ImmutableStatusResponse.class)
public interface StatusResponse {
  Optional<Status> status();

  Optional<String> resultUrl();

  Optional<String> error();
}
