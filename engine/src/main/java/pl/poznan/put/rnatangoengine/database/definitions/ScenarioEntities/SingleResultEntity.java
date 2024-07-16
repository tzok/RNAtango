package pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities;

import jakarta.persistence.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

  private String fileStructureName;
  private String fileStructureMolecule;
  private String fileStructureTitle;
  private String fileId;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "result_selection",
      joinColumns = @JoinColumn(name = "selection_id"),
      inverseJoinColumns = @JoinColumn(name = "result_id"))
  private List<SelectionEntity> selections;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "result_chain_torsion_angle",
      joinColumns = @JoinColumn(name = "chain_torsion_angle_id"),
      inverseJoinColumns = @JoinColumn(name = "result_id"))
  private List<ChainTorsionAngleEntity> chainTorsionAngleEntities;

  private Date removeAfter;
  private Boolean discontinuousResiduesSequence;

  @Column(length = 5000)
  protected String errorLog;

  protected String userErrorLog;
  protected Status status;

  private void setDefaultValues() {
    this.fileStructureName = "";
    this.fileStructureMolecule = "";
    this.chainTorsionAngleEntities = new ArrayList<>();
    this.status = Status.WAITING;
    this.removeAfter = Date.valueOf(LocalDate.now().plus(1, ChronoUnit.WEEKS));
    this.discontinuousResiduesSequence = false;
  }

  public SingleResultEntity() {
    setDefaultValues();
  }

  public SingleResultEntity(List<SelectionEntity> selectionEntities, String fileId) {
    this.fileId = fileId;
    this.selections = selectionEntities;
    setDefaultValues();
  }

  public SingleResultEntity(byte[] structureFileContent) {
    this.structureFileContent = structureFileContent;
    setDefaultValues();
  }

  public SingleResultEntity(List<SelectionEntity> selectionEntities, byte[] structureFileContent) {
    this.structureFileContent = structureFileContent;
    this.selections = selectionEntities;
    setDefaultValues();
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

  public void setUserErrorLog(String userErrorLog) {
    this.userErrorLog = userErrorLog;
  }

  public void setIsDiscontinuousResiduesSequence(Boolean isDiscontinuous) {
    discontinuousResiduesSequence = isDiscontinuous;
  }

  public void setStructureName(String name) {
    this.fileStructureName = name;
  }

  public void setStructureMolecule(String molecule) {
    this.fileStructureMolecule = molecule;
  }

  public String getErrorLog() {
    return this.errorLog;
  }

  public String getUserErrorLog() {
    return this.userErrorLog;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return this.status;
  }

  public String getFileId() {
    return this.fileId;
  }

  public Date getRemoveAfter() {
    return this.removeAfter;
  }

  public String getStructureName() {
    return this.fileStructureName;
  }

  public String getStructureMolecule() {
    return this.fileStructureMolecule;
  }

  public void setStructureTitle(String title) {
    this.fileStructureTitle = title;
  }

  public String getStrucutreTitle() {
    return this.fileStructureTitle;
  }

  public Boolean isDiscontinuousResiduesSequence() {
    return this.discontinuousResiduesSequence;
  }
}
