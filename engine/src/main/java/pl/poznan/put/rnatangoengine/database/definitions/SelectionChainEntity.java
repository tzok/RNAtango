package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.List;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelectionChain;
import pl.poznan.put.rnatangoengine.dto.SelectionChain;

@Entity
@Table(name = "selectionChain")
public class SelectionChainEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  private String name;
  private String sequence;

  private int fromInclusive;

  private int toInclusive;

  @ManyToMany(mappedBy = "selectionChains")
  List<SelectionEntity> selection;

  public SelectionChainEntity() {}

  public SelectionChainEntity(String name, int fromInclusive, int toInclusive) {
    this.name = name;
    this.fromInclusive = fromInclusive;
    this.toInclusive = toInclusive;
    this.sequence = "";
  }

  public String getName() {
    return this.name;
  }

  public int getFromInclusive() {
    return this.fromInclusive;
  }

  public int getToInclusive() {
    return this.toInclusive;
  }

  public String getSequence() {
    return this.sequence;
  }

  public void setSequence(String sequence) {
    this.sequence = sequence;
  }

  public void setFromInclusive(int fromInclusive) {
    this.fromInclusive = fromInclusive;
  }

  public void setToInclusive(int toInclusive) {
    this.toInclusive = toInclusive;
  }

  public SelectionChain getConvertedToSelectionChainImmutable() {
    return ImmutableSelectionChain.builder()
        .name(this.name)
        .nucleotideRange(
            ImmutableNucleotideRange.builder()
                .fromInclusive(this.fromInclusive)
                .toInclusive(this.toInclusive)
                .build())
        .build();
  }

  public Long getId() {
    return id;
  }
}
