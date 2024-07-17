package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.dto.manyMany.ClusteredModel;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableClusteredModel;

@Entity
@Table(name = "clusteredModelEntity")
public class ClusteredModelEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private int clusterNumber;

  private String name;
  private double x;
  private double y;

  public ClusteredModelEntity() {}

  public ClusteredModelEntity(int clusterNumber, String name, double x, double y) {
    this.clusterNumber = clusterNumber;
    this.name = name;
    this.x = x;
    this.y = y;
  }

  public ClusteredModel getClusteredModelImmutable() {
    return ImmutableClusteredModel.builder()
        .x(this.x)
        .y(this.y)
        .clusterNumber(this.clusterNumber)
        .name(this.name)
        .build();
  }
}
