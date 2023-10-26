package pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.ChainTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.definitions.TaskEntity;
import pl.poznan.put.rnatangoengine.dto.Status;

@Entity
@Table(name = "singleResults")
public class SingleResultEntity extends TaskEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  @GeneratedValue(strategy = GenerationType.UUID)
  protected UUID hashId;

  private String structureFileContent;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "selection", referencedColumnName = "id")
  private SelectionEntity selection;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "result_chain_torsion_angle",
      joinColumns = @JoinColumn(name = "chain_torsion_angle_id"),
      inverseJoinColumns = @JoinColumn(name = "result_id"))
  private List<ChainTorsionAngleEntity> chainTorsionAngleEntities;

  public SingleResultEntity(String structureFileContent) {
    this.status = Status.WAITING;
    this.structureFileContent = structureFileContent;
  }

  public SingleResultEntity(SelectionEntity selectionEntity, String structureFileContent) {
    this.status = Status.WAITING;
    this.structureFileContent = structureFileContent;
    this.selection = selectionEntity;
  }

  public Long getId() {
    return id;
  }

  public UUID getHashId() {
    return hashId;
  }

  public List<ChainTorsionAngleEntity> getChainTorsionAngles() {
    return this.chainTorsionAngleEntities;
  }

  public SelectionEntity getSelection() {
    return this.selection;
  }

  public String getStructureFileContent() {
    return this.structureFileContent;
  }

  public void setStructureFileContent(String structureFileContent) {
    this.structureFileContent = structureFileContent;
  }
}
