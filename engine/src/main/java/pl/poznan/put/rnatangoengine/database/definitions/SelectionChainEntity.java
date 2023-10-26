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

  private int fromInclusive;

  private int toInclusive;

  @ManyToMany(mappedBy = "selectionChains")
  List<SelectionEntity> selection;

  public SelectionChainEntity(String name, int fromInclusive, int toInclusive) {
    this.name = name;
    this.fromInclusive = fromInclusive;
    this.toInclusive = toInclusive;
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

  public SelectionChain getConvertedToSelectionChainImmutable() {
    return ImmutableSelectionChain.builder()
        .name(this.name)
        .nucleotideRange(
            ImmutableNucleotideRange.builder()
                .fromInclusive(this.fromInclusive)
                .toInclusive(this.fromInclusive)
                .build())
        .build();
  }

  public Long getId() {
    return id;
  }
}
