package pl.poznan.put.rnatangoengine.logic.singleProcessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.pdb.analysis.*;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.database.definitions.ChainTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ResidueTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.repository.ChainTorsionAngleRepository;
import pl.poznan.put.rnatangoengine.database.repository.ResidueTorsionAngleRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.structure.StructureManager;

@Service
public class SingleProcessing {
  @Autowired SingleResultRepository singleRepository;
  @Autowired ChainTorsionAngleRepository chainTorsionAngleRepository;
  @Autowired ResidueTorsionAngleRepository residueTorsionAngleRepository;

  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureProcessingService structureProcessingService;

  public SingleProcessing() {}

  public void startTask(UUID taskHashId) {

    Structure structure;
    SingleResultEntity singleResultEntity = singleRepository.getByHashId(taskHashId);

    try {
      if (!singleResultEntity.getStatus().equals(Status.WAITING)) {
        return;
      }
      singleResultEntity.setStatus(Status.PROCESSING);
      singleRepository.save(singleResultEntity);

      structure = structureProcessingService.process(singleResultEntity.getFileId());
      singleResultEntity.setStructureFileContent(
          structure
              .filterParseCif(
                  singleResultEntity.getSelections().stream()
                      .map((s) -> s.getConvertedToSelectionImmutable())
                      .collect(Collectors.toList()))
              .getBytes());

      singleRepository.save(singleResultEntity);

      List<ChainTorsionAngleEntity> structureSingleProcessingTorsionAngles =
          new ArrayList<ChainTorsionAngleEntity>();
      ExportAngleNameToAngle exportAngleNameToAngle = new ExportAngleNameToAngle();

      File tempFile = File.createTempFile("structure", ".cif");

      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.getAbsolutePath()));
      writer.write(
          new String(singleResultEntity.getStructureFileContent(), StandardCharsets.UTF_8));
      writer.close();

      PdbModel model =
          StructureManager.loadStructure(tempFile)
              .get(Integer.valueOf(singleResultEntity.getSelections().get(0).getModelName()) - 1);

      for (final PdbChain chain : model.chains()) {
        ChainTorsionAngleEntity chainTorsionAngleEntity =
            new ChainTorsionAngleEntity(chain.identifier(), chain.sequence());
        chainTorsionAngleRepository.save(chainTorsionAngleEntity);
        ImmutablePdbCompactFragment fragment = ImmutablePdbCompactFragment.of(chain.residues());
        List<ResidueTorsionAngleEntity> residueAngles = new ArrayList<ResidueTorsionAngleEntity>();

        for (final PdbResidue residue : fragment.residues()) {
          final ResidueTorsionAngles residueTorsionAngles =
              fragment.torsionAngles(residue.identifier());

          ResidueTorsionAngleEntity _residueTorsionAngleEntity =
              residueTorsionAngleRepository.save(
                  new ResidueTorsionAngleEntity(
                      residue.modifiedResidueName(),
                      residue.residueNumber(),
                      residue.insertionCode().orElse("")));

          MoleculeType.RNA.allAngleTypes().stream()
              .forEach(
                  (residueAngle) ->
                      _residueTorsionAngleEntity.setAngle(
                          exportAngleNameToAngle.parse(residueAngle.exportName()),
                          (residueTorsionAngles.value(residueAngle).isValid()
                              ? residueTorsionAngles.value(residueAngle).degrees()
                              : null)));
          residueAngles.add(_residueTorsionAngleEntity);
        }
        residueTorsionAngleRepository.saveAll(residueAngles);
        chainTorsionAngleEntity.getResiduesTorsionAngles().addAll(residueAngles);

        structureSingleProcessingTorsionAngles.add(chainTorsionAngleEntity);
      }
      chainTorsionAngleRepository.saveAll(structureSingleProcessingTorsionAngles);
      singleResultEntity.getChainTorsionAngles().addAll(structureSingleProcessingTorsionAngles);
      singleResultEntity.setStatus(Status.SUCCESS);
      singleRepository.save(singleResultEntity);
      tempFile.deleteOnExit();

    } catch (IOException e) {
      singleResultEntity.setStatus(Status.FAILED);
      singleResultEntity.setErrorLog(e.getStackTrace().toString());
      singleResultEntity.setUserErrorLog("Error during structure processing");

      singleRepository.save(singleResultEntity);
    } catch (IllegalArgumentException e) {
      singleResultEntity.setStatus(Status.FAILED);
      singleResultEntity.setErrorLog(e.getStackTrace().toString());
      singleResultEntity.setUserErrorLog("Residues have not got atoms coordinates");

      singleRepository.save(singleResultEntity);
    }
  }
}
