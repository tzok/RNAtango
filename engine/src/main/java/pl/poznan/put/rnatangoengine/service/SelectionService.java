package pl.poznan.put.rnatangoengine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.dto.Selection;

@Service
public class SelectionService {
  @Autowired SelectionRepository selectionRepository;
  @Autowired SelectionChainRepository selectionChainRepository;

  public SelectionEntity createSelectionEntity(Selection selection) {
    SelectionEntity selectionEntity = new SelectionEntity(selection.modelName());
    List<SelectionChainEntity> selectionChains = new ArrayList<>();
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
    selectionEntity = selectionRepository.saveAndFlush(selectionEntity);
    selectionEntity.setAllChains(selectionChainRepository.saveAllAndFlush(selectionChains));
    return selectionRepository.saveAndFlush(selectionEntity);
  }
}
