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
import pl.poznan.put.rnatangoengine.database.definitions.ClusteringResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.CommonChainSequenceEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.definitions.WebPushSubscription;
import pl.poznan.put.rnatangoengine.dto.Angle;
import pl.poznan.put.rnatangoengine.dto.Status;

@Entity
@Table(name = "manyManyResults")
public class ManyManyResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  @Convert(converter = AngleListConverter.class)
  private List<Angle> anglesToAnalyze;

  private Double threshold;

  @Column(length = 1000)
  protected String errorLog;

  private Date removeAfter;

  protected String userErrorLog;
  protected Status status;

  private String chain;

  @Column(length = 5000)
  private String finalSequence;

  @Lob private byte[] dendrogram;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "common_sequence",
      joinColumns = @JoinColumn(name = "manymany_id"),
      inverseJoinColumns = @JoinColumn(name = "sequence_id"))
  private List<CommonChainSequenceEntity> commonSequences;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "manymany_models",
      joinColumns = @JoinColumn(name = "manymany_id"),
      inverseJoinColumns = @JoinColumn(name = "model_id"))
  private List<StructureModelEntity> models;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "manymany_compares",
      joinColumns = @JoinColumn(name = "manymany_id"),
      inverseJoinColumns = @JoinColumn(name = "model_id"))
  private List<OneManyResultEntity> oneManyCompares;

  @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinTable(
      name = "manymany_clustering",
      joinColumns = @JoinColumn(name = "manymany_id"),
      inverseJoinColumns = @JoinColumn(name = "clusters_id"))
  private List<ClusteringResultEntity> clusters;

  @ManyToMany(fetch = FetchType.EAGER)
  private List<WebPushSubscription> webPushSubscriptions;

  public ManyManyResultEntity() {
    this.models = new ArrayList<>();
    this.oneManyCompares = new ArrayList<>();
    this.commonSequences = new ArrayList<>();
    this.anglesToAnalyze = new ArrayList<>();
    this.webPushSubscriptions = new ArrayList<>();

    this.clusters = new ArrayList<>();
    this.status = Status.SETTING;
    this.removeAfter = Date.valueOf(LocalDate.now().plus(1, ChronoUnit.WEEKS));
    this.errorLog = "";
    this.userErrorLog = "";
  }

  public void removeModel(StructureModelEntity model) {
    models.remove(model);
  }

  public void setCommonSequences(List<CommonChainSequenceEntity> commonSequences) {
    this.commonSequences = commonSequences;
  }

  public List<CommonChainSequenceEntity> getCommonSequences() {
    return this.commonSequences;
  }

  public void addModel(StructureModelEntity model) {
    this.models.add(model);
  }

  public UUID getHashId() {
    return hashId;
  }

  public List<StructureModelEntity> getModels() {
    return this.models;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return this.status;
  }

  public String getErrorLog() {
    return this.errorLog;
  }

  public String getUserErrorLog() {
    return this.userErrorLog;
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

  public double getProcessingProgess() {
    int successes = 0;
    for (OneManyResultEntity oneManyResultEntity : this.oneManyCompares) {
      if (oneManyResultEntity.getStatus() == Status.SUCCESS) {
        successes += oneManyResultEntity.getProgressValue();
      }
    }

    return successes / (this.models.size() * (this.models.size() - 1));
  }

  public void setUserErrorLog(String userErrorLog) {
    this.userErrorLog = userErrorLog;
  }

  public void setAnglesToAnalyze(List<Angle> angle) {
    this.anglesToAnalyze = angle;
  }

  public List<Angle> getAnglesToAnalyze() {
    return this.anglesToAnalyze;
  }

  public void setThreshold(Double treshold) {
    this.threshold = treshold;
  }

  public Double getThreshold() {
    return threshold;
  }

  public void setChainToAnalyze(String chain) {
    this.chain = chain;
  }

  public String getChainToAnalyze() {
    return chain;
  }

  public void setSequenceToAnalyze(String sequence) {
    this.finalSequence = sequence.toUpperCase();
  }

  public String getSequenceToAnalyze() {
    return finalSequence;
  }

  public void addOneManyInstance(OneManyResultEntity oneManyResultEntity) {
    this.oneManyCompares.add(oneManyResultEntity);
  }

  public List<OneManyResultEntity> getAllComparations() {
    return this.oneManyCompares;
  }

  public Date getRemoveAfter() {
    return this.removeAfter;
  }

  public void addClustering(ClusteringResultEntity clusteringResultEntity) {
    this.clusters.add(clusteringResultEntity);
  }

  public void setDendrogram(byte[] content) {
    this.dendrogram = content;
  }

  public byte[] getDendrogram() {
    return this.dendrogram;
  }

  public List<ClusteringResultEntity> getClustering() {
    return this.clusters;
  }

  public List<WebPushSubscription> getSubscibers() {
    return this.webPushSubscriptions;
  }

  public void setSubscribers(List<WebPushSubscription> subscribers) {
    this.webPushSubscriptions = new ArrayList<>();
    this.webPushSubscriptions = subscribers;
  }
}
