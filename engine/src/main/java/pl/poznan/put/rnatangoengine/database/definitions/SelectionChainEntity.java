package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
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

  @Column(length = 5000)
  private String sequence;

  private int fromInclusive;

  private int toInclusive;

  // @ManyToMany(mappedBy = "selectionChains")
  // List<SelectionEntity> selection;

  public SelectionChainEntity() {}

  public SelectionChainEntity(String name, int fromInclusive, int toInclusive) {
    this.name = name;
    this.fromInclusive = fromInclusive;
    this.toInclusive = toInclusive;
    this.sequence = "";
  }

  public SelectionChainEntity(String name, String sequence, int fromInclusive, int toInclusive) {
    this.name = name;
    this.fromInclusive = fromInclusive;
    this.toInclusive = toInclusive;
    this.sequence = sequence.toUpperCase();
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
    this.sequence = sequence.toUpperCase();
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
        .sequence(this.sequence)
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
