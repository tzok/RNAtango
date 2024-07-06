package pl.poznan.put.rnatangoengine.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionChainRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.FileFormat;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelectionChain;
import pl.poznan.put.rnatangoengine.dto.IndexPair;
import pl.poznan.put.rnatangoengine.dto.Selection;
import pl.poznan.put.rnatangoengine.dto.StructureChainSequence;
import pl.poznan.put.rnatangoengine.dto.StructureComparingResult;
import pl.poznan.put.rnatangoengine.logic.StructureLcs;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.utils.ModelTargetMatchingException;
import pl.poznan.put.rnatangoengine.utils.OneManyUtils;

@Service
public class StructureModelService {
  @Autowired OneManyRepository oneManyRepository;
  @Autowired FileRepository fileRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired StructureLcs structureLcs;
  @Autowired OneManyUtils oneManyUtils;
  @Autowired QueueService queueService;
  @Autowired SelectionChainRepository selectionChainRepository;

  public StructureModelEntity createModelFromUpload(String fileHashId, Selection residueSelection)
      throws Exception {

    Structure structure = structureProcessingService.process(fileHashId);

    fileRepository.deleteByHashId(UUID.fromString(fileHashId));

    byte[] structureFilteredContent = structure.filterParseCif(residueSelection, false).getBytes();
    StructureChainSequence chainSequence =
        structure
            .getContinuousSequences()
            .getChainSubsequence(residueSelection.chains().get(0).name())
            .get(0);
    SelectionEntity selectionEntity =
        new SelectionEntity(
            ImmutableSelection.builder()
                .modelName(residueSelection.modelName())
                .addChains(
                    ImmutableSelectionChain.builder()
                        .name(residueSelection.chains().get(0).name())
                        .nucleotideRange(
                            ImmutableNucleotideRange.builder()
                                .fromInclusive(chainSequence.getFrom())
                                .toInclusive(chainSequence.getTo())
                                .build())
                        .build())
                .build());

    SelectionEntity sourceSelection = selectionRepository.saveAndFlush(selectionEntity);
    StructureModelEntity model =
        new StructureModelEntity(
            structureFilteredContent, structure.getStructureName(), sourceSelection);
    model.setStructureMolecule(structure.getStructureMoleculeName());
    model.setContent(structureFilteredContent);
    model.setSourceSequence(structure.getFirstSequence());
    model.setFilteredSequence(structure.getFirstSequence());

    return structureModelRepository.saveAndFlush(model);
  }

  public StructureModelEntity applyModelTargetCommonSequence(
      StructureModelEntity structureModelEntity, String targetSequence)
      throws ModelTargetMatchingException, Exception {
    Structure structure =
        structureProcessingService.parseStructureFile(
            new String(structureModelEntity.getContent(), StandardCharsets.UTF_8), FileFormat.CIF);
    structure.filterAuthParseCif(
        structureModelEntity.getSourceSelection().getConvertedToSelectionImmutable());
    StructureComparingResult comparingResult =
        structureLcs.compareTargetAndModelSequences(
            targetSequence,
            structure
                .getContinuousSequences()
                .getChainSubsequence(
                    structureModelEntity
                        .getSourceSelection()
                        .getSelectionChains()
                        .get(0)
                        .getName()));
    StructureChainSequence bestSubsequence = comparingResult.getModel();
    SelectionEntity sourceSelectionEntity = structureModelEntity.getSourceSelection();
    sourceSelectionEntity.setSelection(
        ImmutableSelection.builder()
            .modelName("1")
            .addChains(
                ImmutableSelectionChain.builder()
                    .name(
                        structureModelEntity
                            .getSourceSelection()
                            .getSelectionChains()
                            .get(0)
                            .getName())
                    .nucleotideRange(
                        ImmutableNucleotideRange.builder()
                            .fromInclusive(bestSubsequence.getFrom())
                            .toInclusive(bestSubsequence.getTo())
                            .build())
                    .build())
            .build());

    structureModelEntity.setSelection(
        selectionRepository.saveAndFlush(
            new SelectionEntity(
                ImmutableSelection.builder()
                    .modelName("1")
                    .addChains(
                        ImmutableSelectionChain.builder()
                            .name(
                                structureModelEntity
                                    .getSourceSelection()
                                    .getSelectionChains()
                                    .get(0)
                                    .getName())
                            .nucleotideRange(
                                ImmutableNucleotideRange.builder()
                                    .fromInclusive(
                                        bestSubsequence.getFrom()
                                            + comparingResult.getModelFromInclusiveRelative())
                                    .toInclusive(
                                        bestSubsequence.getFrom()
                                            + comparingResult.getModelToInclusiveRelative())
                                    .build())
                            .build())
                    .build())));
    structureModelEntity.setSourceSelection(
        selectionRepository.saveAndFlush(sourceSelectionEntity));
    structureModelEntity.setTargetRangeRelative(
        new IndexPair(
            comparingResult.getTargetFromInclusiveRelative(),
            comparingResult.getTargetToInclusiveRelative()));
    structureModelEntity.setFilteredSequence(comparingResult.getSequence());
    structureModelEntity.setSourceSequence(bestSubsequence.getSequence());
    return structureModelRepository.saveAndFlush(structureModelEntity);
  }

  public StructureModelEntity createModelFromBytes(byte[] content, String filename, String chain)
      throws Exception {
    Structure structure =
        structureProcessingService.process(new String(content, StandardCharsets.UTF_8), filename);

    String structureContent = structure.filterParseCif("1", chain);

    SelectionEntity sourceSelectionEntity =
        selectionRepository.saveAndFlush(
            new SelectionEntity(
                ImmutableSelection.builder()
                    .modelName("1")
                    .addChains(
                        ImmutableSelectionChain.builder()
                            .name(chain)
                            .nucleotideRange(
                                ImmutableNucleotideRange.builder()
                                    .fromInclusive(
                                        structure
                                            .getCifModels()
                                            .get(0)
                                            .residueIdentifiers()
                                            .get(0)
                                            .residueNumber())
                                    .toInclusive(
                                        structure
                                            .getCifModels()
                                            .get(0)
                                            .residueIdentifiers()
                                            .get(
                                                structure
                                                        .getCifModels()
                                                        .get(0)
                                                        .residueIdentifiers()
                                                        .size()
                                                    - 1)
                                            .residueNumber())
                                    .build())
                            .build())
                    .build()));
    StructureModelEntity model =
        new StructureModelEntity(structureContent.getBytes(), filename, sourceSelectionEntity);

    model.setStructureMolecule(structure.getStructureMoleculeName());
    return structureModelRepository.saveAndFlush(model);
  }

  public List<StructureModelEntity> intersectModelsSelectionWithTarget(
      List<StructureModelEntity> models, String targetSequence) {

    for (int i = 0; i < models.size(); i++) {
      StructureModelEntity model = models.get(i);
      int startIndex = model.getFilteredSequence().indexOf(targetSequence);
      SelectionChainEntity selectionChain = model.getSelection().getSelectionChains().get(0);
      selectionChain.setFromInclusive(selectionChain.getFromInclusive() + startIndex);
      selectionChain.setToInclusive(
          selectionChain.getFromInclusive() + startIndex + targetSequence.length() - 1);
      selectionChainRepository.saveAndFlush(selectionChain);
      model.setTargetRangeRelative(new IndexPair(0, targetSequence.length() - 1));
      model.setFilteredSequence(targetSequence);
      models.set(i, model);
    }
    return structureModelRepository.saveAllAndFlush(models);
  }

  public StructureModelEntity filterModelContent(StructureModelEntity model) throws Exception {
    Structure modelStructure =
        structureProcessingService.parseStructureFile(
            new String(model.getContent(), StandardCharsets.UTF_8), FileFormat.CIF);
    model.setContent(
        modelStructure
            .filterAuthParseCif(model.getSelection().getConvertedToSelectionImmutable())
            .getBytes());
    model.setFilteredSequence(modelStructure.getFirstSequence());

    return structureModelRepository.saveAndFlush(model);
  }
}
