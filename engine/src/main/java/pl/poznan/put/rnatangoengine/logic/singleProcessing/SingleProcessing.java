package pl.poznan.put.rnatangoengine.logic.singleProcessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import pl.poznan.put.pdb.analysis.*;
import pl.poznan.put.rnatangoengine.database.definitions.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.repository.SingleRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableTorsionAnglesInChain;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.dto.TorsionAnglesInChain;
import pl.poznan.put.structure.StructureManager;
import pl.poznan.put.torsion.MasterTorsionAngleType;

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
    try {
      PdbModel model =
          StructureManager.loadStructure(singleResultEntity.getStructureFileContent()).get(0);

      for (final PdbChain chain : model.chains()) {

        ImmutablePdbCompactFragment fragment = ImmutablePdbCompactFragment.of(chain.residues());

        for (final PdbResidue residue : fragment.residues()) {
          final ResidueTorsionAngles residueTorsionAngles =
              fragment.torsionAngles(residue.identifier());

          residue.chainIdentifier();
          residue.residueNumber();
          residue.insertionCode().orElse("");
          residue.modifiedResidueName();
          for (final MasterTorsionAngleType angleType : MoleculeType.RNA.allAngleTypes()) {
            angleType.shortDisplayName()
            residueTorsionAngles.value(angleType).degrees();
          }
        }
        results.add(ImmutableTorsionAnglesInChain.builder())
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
