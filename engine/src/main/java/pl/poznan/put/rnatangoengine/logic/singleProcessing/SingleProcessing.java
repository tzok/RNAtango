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
import pl.poznan.put.rna.NucleotideTorsionAngle;
import pl.poznan.put.rnatangoengine.WebPushService;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.database.definitions.ChainTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ResidueTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.repository.ChainTorsionAngleRepository;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
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
  @Autowired FileRepository fileRepository;
  @Autowired WebPushService webPushService;

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
      singleResultEntity.setStructureName(structure.getStructureName());
      singleResultEntity.setStructureMolecule(structure.getStructureMoleculeName());
      singleResultEntity.setStructureTitle(structure.getStrucutreTitle());
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

      for (SelectionEntity selection : singleResultEntity.getSelections()) {
        PdbModel model =
            StructureManager.loadStructure(tempFile)
                .get(Integer.valueOf(selection.getModelName()) - 1);
        for (final PdbChain chain : model.chains()) {
          ChainTorsionAngleEntity chainTorsionAngleEntity =
              new ChainTorsionAngleEntity(chain.identifier(), chain.sequence());
          chainTorsionAngleRepository.save(chainTorsionAngleEntity);
          ImmutablePdbCompactFragment fragment = ImmutablePdbCompactFragment.of(chain.residues());
          List<ResidueTorsionAngleEntity> residueAngles =
              new ArrayList<ResidueTorsionAngleEntity>();

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
            _residueTorsionAngleEntity.setAngle(
                exportAngleNameToAngle.parse(
                    NucleotideTorsionAngle.PSEUDOPHASE_PUCKER.exportName()),
                residueTorsionAngles.value(NucleotideTorsionAngle.PSEUDOPHASE_PUCKER).isValid()
                    ? residueTorsionAngles
                        .value(NucleotideTorsionAngle.PSEUDOPHASE_PUCKER)
                        .degrees()
                    : null);
            residueAngles.add(_residueTorsionAngleEntity);
          }

          chainTorsionAngleEntity.getResiduesTorsionAngles().addAll(residueAngles);

          structureSingleProcessingTorsionAngles.add(chainTorsionAngleEntity);
        }
      }
      singleResultEntity.getChainTorsionAngles().addAll(structureSingleProcessingTorsionAngles);

      singleResultEntity.setStatus(Status.SUCCESS);
      singleResultEntity.setIsDiscontinuousResiduesSequence(
          structure.getContainDiscontinuousScopes());
      singleRepository.save(singleResultEntity);
      tempFile.deleteOnExit();
      try {
        fileRepository.deleteByHashId(UUID.fromString(singleResultEntity.getFileId()));
      } catch (Exception e) {
      }
    } catch (IOException e) {
      e.printStackTrace();

      singleResultEntity.setStatus(Status.FAILED);
      singleResultEntity.setErrorLog(e.getStackTrace().toString());
      singleResultEntity.setUserErrorLog("Error during structure processing");
      singleResultEntity
          .getSubscibers()
          .forEach(
              (s) ->
                  webPushService.sendNotificationToClient(
                      s,
                      "Single model task "
                          + singleResultEntity.getHashId().toString()
                          + " completed"));
      singleRepository.save(singleResultEntity);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      singleResultEntity.setStatus(Status.FAILED);
      singleResultEntity.setErrorLog(e.getStackTrace().toString());
      singleResultEntity.setUserErrorLog("Residues does not have atoms coordinates");
      singleResultEntity
          .getSubscibers()
          .forEach(
              (s) ->
                  webPushService.sendNotificationToClient(
                      s,
                      "Single model task "
                          + singleResultEntity.getHashId().toString()
                          + " processing failed"));
      singleRepository.save(singleResultEntity);
    }
  }
}
