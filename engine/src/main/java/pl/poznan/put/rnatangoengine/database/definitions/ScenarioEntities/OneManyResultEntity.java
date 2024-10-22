package pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities;

import jakarta.persistence.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.converters.AngleListConverter;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.definitions.WebPushSubscription;
import pl.poznan.put.rnatangoengine.dto.Angle;
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

  @Column(length = 1000)
  protected String errorLog;

  protected String userErrorLog;
  protected Status status;

  private Double threshold;

  private String finalSequence;
  private String finalStructure;

  private double progress;

  @Convert(converter = AngleListConverter.class)
  private List<Angle> anglesToAnalyze;

  @OneToOne
  @JoinColumn(name = "structure_target_id", insertable = true, updatable = true, nullable = true)
  private StructureModelEntity target;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "onemany_models",
      joinColumns = @JoinColumn(name = "onemany_id"),
      inverseJoinColumns = @JoinColumn(name = "model_id"))
  private List<StructureModelEntity> models;

  @ManyToMany(fetch = FetchType.EAGER)
  private List<WebPushSubscription> webPushSubscriptions;

  private void setDefaultValues() {
    this.status = Status.SETTING;
    this.removeAfter = Date.valueOf(LocalDate.now().plus(1, ChronoUnit.WEEKS));
    this.discontinuousResiduesSequence = false;
    this.errorLog = "";
    this.userErrorLog = "";
    this.progress = 0;
    this.webPushSubscriptions = new ArrayList<>();

    this.models = new ArrayList<>();
  }

  public OneManyResultEntity() {
    setDefaultValues();
  }

  public OneManyResultEntity(StructureModelEntity target, String modelNumber, String chain) {
    this.model = modelNumber;
    this.chain = chain;
    this.target = target;
    setDefaultValues();
  }

  public void setAnglesToAnalyze(List<Angle> angle) {
    this.anglesToAnalyze = angle;
  }

  public List<Angle> getAnglesToAnalyze() {
    return this.anglesToAnalyze;
  }

  public void incrementProgress() {
    this.progress += 1;
  }

  public double getProgressValue() {
    return this.progress;
  }

  public double getProcessingProgess() {
    return this.progress / (double) this.models.size();
  }

  public void setThreshold(Double treshold) {
    this.threshold = treshold;
  }

  public Double getThreshold() {
    return threshold;
  }

  public String getChain() {
    return this.chain;
  }

  public UUID getHashId() {
    return hashId;
  }

  public String getModelNumber() {
    return model;
  }

  public void setErrorLog(String errorLog) {
    this.errorLog = errorLog.substring(0, Math.min(errorLog.length(), 1000));
  }

  public void setErrorLog(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    setErrorLog(sw.toString());
  }

  public void setUserErrorLog(String userErrorLog) {
    this.userErrorLog = userErrorLog;
  }

  public void setIsDiscontinuousResiduesSequence(Boolean isDiscontinuous) {
    discontinuousResiduesSequence = isDiscontinuous;
  }

  public void setFinalStructure(String structure) {
    this.finalStructure = structure;
  }

  public void setFinalSequence(String sequence) {
    this.finalSequence = sequence;
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

  public void removeModel(StructureModelEntity model) {
    models.remove(model);
  }

  public List<StructureModelEntity> getModels() {
    return models;
  }

  public void addModel(StructureModelEntity model) {
    models.add(model);
  }

  public void addAllModels(List<StructureModelEntity> models) {
    models.addAll(models);
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

  public List<WebPushSubscription> getSubscibers() {
    return this.webPushSubscriptions;
  }

  public void setSubscribers(List<WebPushSubscription> subscribers) {
    this.webPushSubscriptions = new ArrayList<>();
    this.webPushSubscriptions = subscribers;
  }
}
