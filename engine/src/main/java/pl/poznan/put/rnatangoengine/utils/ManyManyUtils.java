package pl.poznan.put.rnatangoengine.utils;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureModelResponse;
import pl.poznan.put.rnatangoengine.dto.IndexPair;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableLongestChainSequence;
import pl.poznan.put.rnatangoengine.dto.manyMany.ImmutableManyManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.manyMany.ManyManySetFormResponse;

@Service
public class ManyManyUtils {
  @Autowired SelectionChainRepository selectionChainRepository;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired OneManyRepository oneManyRepository;
  @Autowired SelectionRepository selectionRepository;

  public OneManyResultEntity applyCommonSubsequenceToTarget(
      OneManyResultEntity _oneManyResultEntity) {
    StructureModelEntity target = _oneManyResultEntity.getTargetEntity();
    List<StructureModelEntity> models = _oneManyResultEntity.getModels();
    IndexPair localModelIndexes;
    IndexPair indexPair;
    try {
      indexPair = models.get(0).getTargetRangeRelative();
    } catch (Exception e) {
      indexPair = new IndexPair(0, target.getSourceSequence().length() - 1);
    }
    for (StructureModelEntity structureModelEntity : models) {
      localModelIndexes = structureModelEntity.getTargetRangeRelative();
      if (indexPair.toInclusive > localModelIndexes.toInclusive) {
        indexPair.toInclusive = localModelIndexes.toInclusive;
      }
      if (indexPair.fromInclusive < localModelIndexes.fromInclusive) {
        indexPair.fromInclusive = localModelIndexes.fromInclusive;
      }
    }
    SelectionChainEntity selectionChainEntity = target.getSelection().getSelectionChains().get(0);

    SelectionChainEntity sourceSelectionChainEntity =
        target.getSourceSelection().getSelectionChains().get(0);
    selectionChainEntity.setToInclusive(
        sourceSelectionChainEntity.getFromInclusive() + indexPair.toInclusive);
    selectionChainEntity.setFromInclusive(
        sourceSelectionChainEntity.getFromInclusive() + indexPair.fromInclusive);
    target.setFilteredSequence(
        target.getSourceSequence().substring(indexPair.fromInclusive, indexPair.toInclusive));

    selectionChainRepository.saveAndFlush(selectionChainEntity);
    structureModelRepository.saveAndFlush(target);
    return oneManyRepository.saveAndFlush(_oneManyResultEntity);
  }

  public ManyManySetFormResponse buildFormStateResponse(
      ManyManyResultEntity _manyManyResultEntity) {
    return ImmutableManyManySetFormResponse.builder()
        .taskHashId(_manyManyResultEntity.getHashId().toString())
        .addAllModels(
            _manyManyResultEntity.getModels().stream()
                .map(
                    (modelI) ->
                        ImmutableStructureModelResponse.builder()
                            .fileId(modelI.getHashId().toString())
                            .fileName(modelI.getFilename())
                            .sequence("")
                            .sourceSelection(
                                ImmutableSelection.builder()
                                    .from(
                                        modelI
                                            .getSourceSelection()
                                            .getConvertedToSelectionImmutable())
                                    .build())
                            .build())
                .collect(Collectors.toList()))
        .addAllSequences(
            _manyManyResultEntity.getCommonSequences().stream()
                .map(
                    s ->
                        ImmutableLongestChainSequence.builder()
                            .sequence(s.getSequence())
                            .name(s.getChain())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }
}
