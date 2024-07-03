package pl.poznan.put.rnatangoengine.utils;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
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

  public void applyCommonSubsequenceToTarget(OneManyResultEntity _oneManyResultEntity) {
    List<StructureModelEntity> models = _oneManyResultEntity.getModels();
    IndexPair localModelIndexes;
    IndexPair indexPair = models.get(0).getTargetRangeRelative();
    for (StructureModelEntity structureModelEntity : models) {
      localModelIndexes = structureModelEntity.getTargetRangeRelative();
      if (indexPair.toInclusive > localModelIndexes.toInclusive) {
        indexPair.toInclusive = localModelIndexes.toInclusive;
      }
      if (indexPair.fromInclusive < localModelIndexes.fromInclusive) {
        indexPair.fromInclusive = localModelIndexes.fromInclusive;
      }
    }
    StructureModelEntity target = _oneManyResultEntity.getTargetEntity();
    SelectionEntity targetSelectionEntity = target.getSelection();
    SelectionEntity targetSourceSelectionEntity = target.getSourceSelection();
    SelectionChainEntity selectionChainEntity = targetSelectionEntity.getSelectionChains().get(0);
    SelectionChainEntity sourceSelectionChainEntity =
        targetSourceSelectionEntity.getSelectionChains().get(0);
    selectionChainEntity.setToInclusive(
        sourceSelectionChainEntity.getFromInclusive() + indexPair.toInclusive);
    selectionChainEntity.setFromInclusive(
        sourceSelectionChainEntity.getFromInclusive() + indexPair.fromInclusive);
    target.setFilteredSequence(
        target.getSourceSequence().substring(indexPair.fromInclusive, indexPair.toInclusive));
    // selectionChainEntity.setSequence(
    // target.getSourceSequence().substring(indexPair.fromInclusive,
    // indexPair.toInclusive));

    selectionChainRepository.saveAndFlush(selectionChainEntity);
    structureModelRepository.saveAndFlush(target);
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
