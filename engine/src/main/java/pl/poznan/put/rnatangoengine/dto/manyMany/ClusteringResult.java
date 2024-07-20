package pl.poznan.put.rnatangoengine.dto.manyMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableClusteringResult.class)
@JsonDeserialize(as = ImmutableClusteringResult.class)
public interface ClusteringResult {

  int numberClusters();

  List<ClusteredModel> models();
}
