package pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities;

import jakarta.persistence.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.dto.Status;

@Entity
@Table(name = "oneManyResults")
public class OneManyResultEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  protected UUID hashId;

  private Date removeAfter;
  private Boolean discontinuousResiduesSequence;

  private String model;
  private String chain;

  protected String errorLog;
  protected String userErrorLog;
  protected Status status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "structure_target_id", insertable = true, updatable = true, nullable = true)
  private StructureModelEntity target;

  @ManyToMany
  @JoinTable(
      name = "onemany_models",
      joinColumns = @JoinColumn(name = "onemany_id"),
      inverseJoinColumns = @JoinColumn(name = "model_id"))
  private List<StructureModelEntity> models;

  private void setDefaultValues() {
    this.status = Status.SETTING;
    this.removeAfter = Date.valueOf(LocalDate.now().plus(1, ChronoUnit.WEEKS));
    this.discontinuousResiduesSequence = false;
  }

  public OneManyResultEntity() {
    setDefaultValues();
  }

  public OneManyResultEntity(StructureModelEntity target, SelectionEntity selectionEntity) {
    this.model = selectionEntity.getModelName();
    this.chain =
        selectionEntity
            .getSelectionChains()
            .get(Integer.getInteger(selectionEntity.getModelName()) - 1)
            .getName();
    this.target = target;
    setDefaultValues();
  }

  public String getChain() {
    return this.chain;
  }

  public UUID getHashId() {
    return hashId;
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

  public List<StructureModelEntity> getModels() {
    return models;
  }

  public void addModel(StructureModelEntity model) {
    models.add(model);
  }

  public StructureModelEntity getTargetEntity() {
    return target;
  }

  public void setTargetEntity(StructureModelEntity target) {
    this.target = target;
  }

  public Date getRemoveAfter() {
    return this.removeAfter;
  }

  public Boolean isDiscontinuousResiduesSequence() {
    return this.discontinuousResiduesSequence;
  }
}
