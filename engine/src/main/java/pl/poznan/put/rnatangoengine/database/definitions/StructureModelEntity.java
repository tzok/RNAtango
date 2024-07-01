package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
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

  @Lob private byte[] sourceContent;
  @Lob private byte[] filteredContent;

  public StructureModelEntity() {}

  public StructureModelEntity(FileEntity file, SelectionEntity selection) {
    this.sourceContent = file.getContent();
    this.filename = file.getFilename();
    this.targetSourceSelection = selection;
    this.selection = selection;
  }

  public StructureModelEntity(byte[] structureContent, String filename, SelectionEntity selection) {
    this.sourceContent = structureContent;
    this.filename = filename;
    this.targetSourceSelection = selection;
    this.selection = selection;
  }

  @ManyToOne
  @JoinColumn(name = "model_sequence_selection_id", nullable = true)
  private SelectionEntity selection; // it means original selection

  @ManyToOne
  @JoinColumn(name = "target_sequence_selection_id", nullable = true)
  private SelectionEntity targetSourceSelection; // it means filtered selection

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

  public void setSourceContent(byte[] content) {
    this.sourceContent = content;
  }

  public void setFilteredContent(byte[] content) {
    this.filteredContent = content;
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

  public byte[] getSourceContent() {
    return sourceContent;
  }

  public byte[] getFilteredContent() {
    return filteredContent;
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

  public void setTargetSelection(SelectionEntity selectionEntity) {
    this.targetSourceSelection = selectionEntity;
  }

  public SelectionEntity getTargetSourceSelection() {
    return targetSourceSelection;
  }
}