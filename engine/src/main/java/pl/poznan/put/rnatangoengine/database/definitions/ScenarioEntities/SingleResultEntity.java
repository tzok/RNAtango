package pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.ChainTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.dto.Status;

@Entity
@Table(name = "singleResults")
public class SingleResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  protected UUID hashId;

  @Lob private byte[] structureFileContent;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "result_selection",
      joinColumns = @JoinColumn(name = "selection_id"),
      inverseJoinColumns = @JoinColumn(name = "result_id"))
  private List<SelectionEntity> selections;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(
      name = "result_chain_torsion_angle",
      joinColumns = @JoinColumn(name = "chain_torsion_angle_id"),
      inverseJoinColumns = @JoinColumn(name = "result_id"))
  private List<ChainTorsionAngleEntity> chainTorsionAngleEntities;

  protected String errorLog;
  protected Status status;

  public SingleResultEntity() {}

  public SingleResultEntity(byte[] structureFileContent) {
    this.chainTorsionAngleEntities = new ArrayList<>();
    this.status = Status.WAITING;
    this.structureFileContent = structureFileContent;
  }

  public SingleResultEntity(List<SelectionEntity> selectionEntities, byte[] structureFileContent) {
    this.chainTorsionAngleEntities = new ArrayList<>();
    this.status = Status.WAITING;
    this.structureFileContent = structureFileContent;
    this.selections = selectionEntities;
  }

  public UUID getHashId() {
    return hashId;
  }

  public List<ChainTorsionAngleEntity> getChainTorsionAngles() {
    return this.chainTorsionAngleEntities;
  }

  public List<SelectionEntity> getSelections() {
    return this.selections;
  }

  public byte[] getStructureFileContent() {
    return this.structureFileContent;
  }

  public void setStructureFileContent(byte[] structureFileContent) {
    this.structureFileContent = structureFileContent;
  }

  public void setErrorLog(String errorLog) {
    this.errorLog = errorLog;
  }

  public String getErrorLog() {
    return this.errorLog;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return this.status;
  }
}
