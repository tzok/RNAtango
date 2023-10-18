package pl.poznan.put.rnatangoengine.logic.singleProcessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import pl.poznan.put.pdb.analysis.*;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.database.definitions.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.repository.SingleRepository;
import pl.poznan.put.rnatangoengine.dto.AngleValue;
import pl.poznan.put.rnatangoengine.dto.ImmutableAngleValue;
import pl.poznan.put.rnatangoengine.dto.ImmutableChain;
import pl.poznan.put.rnatangoengine.dto.ImmutableResidue;
import pl.poznan.put.rnatangoengine.dto.ImmutableTorsionAnglesInChain;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.TorsionAnglesInChain;
import pl.poznan.put.structure.StructureManager;

public class SingleProcessing {
  @Autowired SingleRepository singleRepository;
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
    List<TorsionAnglesInChain> results = new ArrayList<TorsionAnglesInChain>();
    ExportAngleNameToAngle exportAngleNameToAngle = new ExportAngleNameToAngle();
    try {
      PdbModel model =
          StructureManager.loadStructure(singleResultEntity.getStructureFileContent()).get(0);
      for (final PdbChain chain : model.chains()) {

        ImmutablePdbCompactFragment fragment = ImmutablePdbCompactFragment.of(chain.residues());
        List<List<AngleValue>> angleValues = new ArrayList<List<AngleValue>>();

        for (final PdbResidue residue : fragment.residues()) {
          final ResidueTorsionAngles residueTorsionAngles =
              fragment.torsionAngles(residue.identifier());
          angleValues.add(
              MoleculeType.RNA.allAngleTypes().stream()
                  .map(
                      (residueAngle) ->
                          ImmutableAngleValue.builder()
                              .value(residueTorsionAngles.value(residueAngle).degrees())
                              .angle(exportAngleNameToAngle.parse(residueAngle.exportName()))
                              .build())
                  .collect(Collectors.toList()));
        }
        results.add(
            ImmutableTorsionAnglesInChain.builder()
                .addAllValues(angleValues)
                .addAllResidues(
                    fragment.residues().stream()
                        .map(
                            (residue) ->
                                ImmutableResidue.builder()
                                    .number(residue.residueNumber())
                                    .name(residue.chainIdentifier())
                                    .icode(residue.insertionCode().orElse(""))
                                    .build())
                        .collect(Collectors.toList()))
                .chain(
                    ImmutableChain.builder()
                        .name(chain.identifier())
                        .sequence(chain.sequence())
                        .build())
                .build());
      }
      singleResultEntity.setTorsionAngles(results);
      singleResultEntity.setStatus(Status.SUCCESS);

    } catch (IOException e) {
      singleResultEntity.setStatus(Status.FAILED);
      singleResultEntity.setErrorLog(e.getStackTrace().toString());
    }
  }
}
