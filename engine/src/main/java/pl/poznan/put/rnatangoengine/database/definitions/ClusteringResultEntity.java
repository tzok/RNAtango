package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import pl.poznan.put.rnatangoengine.dto.manyMany.ClusteringResult;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableClusteringResult;

@Entity
@Table(name = "clusteringResultEntity")
public class ClusteringResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private int numberClusters;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "cluster_models",
      joinColumns = @JoinColumn(name = "cluster_id"),
      inverseJoinColumns = @JoinColumn(name = "model_id"))
  private List<ClusteredModelEntity> models;

  public ClusteringResultEntity() {}

  public ClusteringResultEntity(int numberClusters) {
    this.numberClusters = numberClusters;
    this.models = new ArrayList<>();
  }

  public void setModels(List<ClusteredModelEntity> models) {
    this.models.addAll(models);
  }

  public ClusteringResult getClusteringResultImmutable() {
    return ImmutableClusteringResult.builder()
        .numberClusters(this.numberClusters)
        .addAllModels(
            this.models.stream()
                .map((model) -> model.getClusteredModelImmutable())
                .collect(Collectors.toList()))
        .build();
  }
}
