package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.Selection;

@Entity
@Table(name = "selection")
public class SelectionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  private String modelName;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  @JoinTable(
      name = "selection_selection_chain",
      joinColumns = @JoinColumn(name = "selection_chain_id"),
      inverseJoinColumns = @JoinColumn(name = "selection_id"))
  private List<SelectionChainEntity> selectionChains;

  public SelectionEntity() {}

  public SelectionEntity(String modelName) {
    this.modelName = modelName;
  }

  public SelectionEntity(Selection selection) {
    setSelection(selection);
  }

  public void setSelection(Selection selection) {
    this.modelName = selection.modelName();
    this.selectionChains = new ArrayList<>();
    selectionChains.addAll(
        selection.chains().stream()
            .map(
                (selectionChain) ->
                    new SelectionChainEntity(
                        selectionChain.name(),
                        selectionChain.sequence().orElse(""),
                        selectionChain.nucleotideRange().fromInclusive(),
                        selectionChain.nucleotideRange().toInclusive()))
            .collect(Collectors.toList()));
  }

  public List<SelectionChainEntity> getSelectionChains() {
    return this.selectionChains;
  }

  public Selection getConvertedToSelectionImmutable() {
    return ImmutableSelection.builder()
        .chains(
            this.selectionChains.stream()
                .map((selectionChain) -> selectionChain.getConvertedToSelectionChainImmutable())
                .collect(Collectors.toList()))
        .modelName(this.modelName)
        .build();
  }

  public String getModelName() {
    return modelName;
  }

  public Long getId() {
    return id;
  }
}
