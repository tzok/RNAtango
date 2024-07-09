package pl.poznan.put.rnatangoengine.utils;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableOneManySetFormResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureModelResponse;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureTargetResponse;
import pl.poznan.put.rnatangoengine.dto.IndexPair;
import pl.poznan.put.rnatangoengine.dto.OneManySetFormResponse;

@Service
public class OneManyUtils {
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

  public OneManySetFormResponse buildFormStateResponse(OneManyResultEntity _oneManyResultEntity) {
    return ImmutableOneManySetFormResponse.builder()
        .target(
            ImmutableStructureTargetResponse.builder()
                .sequence(_oneManyResultEntity.getTargetEntity().getSourceSequence())
                .sourceSelection(
                    ImmutableSelection.builder()
                        .from(
                            _oneManyResultEntity
                                .getTargetEntity()
                                .getSourceSelection()
                                .getConvertedToSelectionImmutable())
                        .build())
                .selection(
                    ImmutableSelection.builder()
                        .from(
                            _oneManyResultEntity
                                .getTargetEntity()
                                .getSelection()
                                .getConvertedToSelectionImmutable())
                        .build())
                .build())
        .models(
            _oneManyResultEntity.getModels().stream()
                .map(
                    (modelI) ->
                        ImmutableStructureModelResponse.builder()
                            .fileId(modelI.getHashId().toString())
                            .sequence(modelI.getFilteredSequence())
                            .fileName(modelI.getFilename())
                            .sourceSelection(
                                ImmutableSelection.builder()
                                    .from(
                                        modelI
                                            .getSourceSelection()
                                            .getConvertedToSelectionImmutable())
                                    .build())
                            .selection(
                                ImmutableSelection.builder()
                                    .from(modelI.getSelection().getConvertedToSelectionImmutable())
                                    .build())
                            .build())
                .collect(Collectors.toList()))
        .build();
  }
}
