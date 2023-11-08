package pl.poznan.put.rnatangoengine.logic.singleProcessing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import pl.poznan.put.pdb.analysis.*;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.database.definitions.ChainTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ResidueTorsionAngleEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.repository.ChainTorsionAngleRepository;
import pl.poznan.put.rnatangoengine.database.repository.ResidueTorsionAngleRepository;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.structure.StructureManager;

public class SingleProcessing {
  @Autowired SingleResultRepository singleRepository;
  @Autowired ChainTorsionAngleRepository chainTorsionAngleRepository;
  @Autowired ResidueTorsionAngleRepository residueTorsionAngleRepository;

  private UUID taskHashId;

  public SingleProcessing(UUID taskHashId) {
    this.taskHashId = taskHashId;
  }

  public void startTask() {
    SingleResultEntity singleResultEntity = singleRepository.getByHashId(taskHashId);

    if (!singleResultEntity.getStatus().equals(Status.WAITING)) {
      return;
    }

    singleResultEntity.setStatus(Status.PROCESSING);
    List<ChainTorsionAngleEntity> structureSingleProcessingTorsionAngles =
        new ArrayList<ChainTorsionAngleEntity>();
    ExportAngleNameToAngle exportAngleNameToAngle = new ExportAngleNameToAngle();

    try {
      File tempFile = File.createTempFile("structure", ".cif");

      BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile.getAbsolutePath()));
      writer.write(singleResultEntity.getStructureFileContent());
      writer.close();

      PdbModel model =
          StructureManager.loadStructure(tempFile)
              .get(Integer.valueOf(singleResultEntity.getSelections().get(0).getModelName()));

      for (final PdbChain chain : model.chains()) {
        ChainTorsionAngleEntity chainTorsionAngleEntity =
            new ChainTorsionAngleEntity(chain.identifier(), chain.sequence());
        ImmutablePdbCompactFragment fragment = ImmutablePdbCompactFragment.of(chain.residues());
        List<ResidueTorsionAngleEntity> residueAngles = new ArrayList<ResidueTorsionAngleEntity>();

        for (final PdbResidue residue : fragment.residues()) {
          final ResidueTorsionAngles residueTorsionAngles =
              fragment.torsionAngles(residue.identifier());

          ResidueTorsionAngleEntity _residueTorsionAngleEntity =
              residueTorsionAngleRepository.save(
                  new ResidueTorsionAngleEntity(
                      residue.chainIdentifier(),
                      residue.residueNumber(),
                      residue.insertionCode().orElse("")));

          MoleculeType.RNA.allAngleTypes().stream()
              .forEach(
                  (residueAngle) ->
                      _residueTorsionAngleEntity.setAngle(
                          exportAngleNameToAngle.parse(residueAngle.exportName()),
                          residueTorsionAngles.value(residueAngle).degrees()));
          residueAngles.add(_residueTorsionAngleEntity);
        }
        residueTorsionAngleRepository.saveAll(residueAngles);
        chainTorsionAngleEntity.getResiduesTorsionAngles().addAll(residueAngles);

        structureSingleProcessingTorsionAngles.add(chainTorsionAngleEntity);
      }
      chainTorsionAngleRepository.saveAll(structureSingleProcessingTorsionAngles);
      singleResultEntity.getChainTorsionAngles().addAll(structureSingleProcessingTorsionAngles);
      singleResultEntity.setStatus(Status.SUCCESS);
      tempFile.deleteOnExit();

    } catch (IOException e) {
      singleResultEntity.setStatus(Status.FAILED);
      singleResultEntity.setErrorLog(e.getStackTrace().toString());
    }
  }
}
