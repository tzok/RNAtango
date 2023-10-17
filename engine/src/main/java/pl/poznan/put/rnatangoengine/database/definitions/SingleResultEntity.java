package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.database.converters.SelectionConverter;
import pl.poznan.put.rnatangoengine.database.converters.TorsionAnglesInChainListConverter;
import pl.poznan.put.rnatangoengine.dto.Selection;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.TorsionAnglesInChain;

@Entity
@Table(name = "singleResults")
public class SingleResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private String structureFileContent;

  @Convert(converter = SelectionConverter.class)
  @Column(name = "selection", nullable = false)
  private Selection selection;

  private Status status;

  @Convert(converter = TorsionAnglesInChainListConverter.class)
  @Column(name = "torsionAnglesInChain", nullable = false)
  private List<TorsionAnglesInChain> torsionAngles;

  public Long getId() {
    return id;
  }

  public UUID getHashId() {
    return hashId;
  }

  public SingleResultEntity(Selection selection, String structureFileContent) {
    this.status = Status.WAITING;
    this.torsionAngles = new ArrayList<TorsionAnglesInChain>();
    this.structureFileContent = structureFileContent;
    this.selection = selection;
  }

  public List<TorsionAnglesInChain> getTorsionAngles() {
    return torsionAngles;
  }

  public void setTorsionAngles(List<TorsionAnglesInChain> torsionAngles) {
    this.torsionAngles = torsionAngles;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return this.status;
  }

  public String getStructureFileContent() {
    return this.structureFileContent;
  }

  public void setStructureFileContent(String structureFileContent) {
    this.structureFileContent = structureFileContent;
  }
}
