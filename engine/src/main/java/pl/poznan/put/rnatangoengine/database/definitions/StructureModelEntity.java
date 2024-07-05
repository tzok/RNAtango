package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.dto.IndexPair;

@Entity
@Table(name = "structure_model_entity")
public class StructureModelEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  protected UUID hashId;

  private String filename;

  private String fileStructureName;
  private String fileStructureMolecule;

  private String sourceSequence;
  private String filteredSequence;

  private Integer toInclusiveTargetRelative;
  private Integer fromInclusiveTargetRelative;

  @Lob private byte[] content;
  @Lob private byte[] secondaryStructureVisualizationSVG;

  private Double mcq;

  public StructureModelEntity() {}

  public StructureModelEntity(FileEntity file, SelectionEntity selection) {
    this.content = file.getContent();
    this.filename = file.getFilename();
    this.sourceSelection = selection;
  }

  public StructureModelEntity(byte[] structureContent, String filename, SelectionEntity selection) {
    this.content = structureContent;
    this.filename = filename;
    this.sourceSelection = selection;
  }

  public StructureModelEntity(
      byte[] structureContent,
      String filename,
      SelectionEntity selection,
      SelectionEntity sourceSelection) {
    this.content = structureContent;
    this.filename = filename;
    this.sourceSelection = sourceSelection;
    this.selection = selection;
  }

  @ManyToOne
  @JoinColumn(name = "model_sequence_selection_id", nullable = true)
  private SelectionEntity selection; // it means filtered selection

  @ManyToOne
  @JoinColumn(name = "source_sequence_selection_id", nullable = true)
  private SelectionEntity sourceSelection; // it means original selection

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  List<ResidueTorsionAngleEntity> residuesTorsionAngleEntities;

  public void setTargetRangeRelative(IndexPair indexPair) {
    this.toInclusiveTargetRelative = indexPair.toInclusive;
    this.fromInclusiveTargetRelative = indexPair.fromInclusive;
  }

  public IndexPair getTargetRangeRelative() {
    return new IndexPair(this.fromInclusiveTargetRelative, this.toInclusiveTargetRelative);
  }

  public void setStructureMolecule(String molecule) {
    this.fileStructureMolecule = molecule;
  }

  public void setContent(byte[] content) {
    this.content = content;
  }

  public void setMcqValue(Double mcq) {
    this.mcq = mcq;
  }

  public Double getMcqValue() {
    return mcq;
  }

  public void setSecondaryStructureVisualizationSVG(byte[] content) {
    this.secondaryStructureVisualizationSVG = content;
  }

  public byte[] getSecondaryStructureVisualizationSVG() {
    return this.secondaryStructureVisualizationSVG;
  }

  public void setFilteredSequence(String sequence) {
    this.filteredSequence = sequence;
  }

  public void setSourceSequence(String sequence) {
    this.sourceSequence = sequence;
  }

  public String getSourceSequence() {
    return this.sourceSequence;
  }

  public String getFilteredSequence() {
    return this.filteredSequence;
  }

  public UUID getHashId() {
    return hashId;
  }

  public byte[] getContent() {
    return content;
  }

  public String getFilename() {
    return filename;
  }

  public void setSelection(SelectionEntity selection) {
    this.selection = selection;
  }

  public SelectionEntity getSelection() {
    return selection;
  }

  public void setSourceSelection(SelectionEntity selectionEntity) {
    this.sourceSelection = selectionEntity;
  }

  public SelectionEntity getSourceSelection() {
    return sourceSelection;
  }

  public List<ResidueTorsionAngleEntity> getTorsionAngleEntities() {
    return residuesTorsionAngleEntities;
  }

  public void addResidueEntities(List<ResidueTorsionAngleEntity> residueTorsionAngleEntities) {
    residuesTorsionAngleEntities.addAll(residueTorsionAngleEntities);
  }
}
