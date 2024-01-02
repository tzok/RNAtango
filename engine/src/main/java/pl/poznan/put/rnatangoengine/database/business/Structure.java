package pl.poznan.put.rnatangoengine.database.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import pl.poznan.put.pdb.PdbAtomLine;
import pl.poznan.put.pdb.analysis.CifModel;
import pl.poznan.put.pdb.analysis.ImmutableDefaultCifModel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.rnatangoengine.dto.Chain;
import pl.poznan.put.rnatangoengine.dto.ImmutableChain;
import pl.poznan.put.rnatangoengine.dto.ImmutableModel;
import pl.poznan.put.rnatangoengine.dto.Model;
import pl.poznan.put.rnatangoengine.dto.Selection;
import pl.poznan.put.rnatangoengine.dto.SelectionChain;

public class Structure {
  List<CifModel> structureModels;

  public Structure(List<CifModel> structureModels) {
    this.structureModels = structureModels;
  }

  /**
   * @return
   * @throws IOException
   */
  public List<CifModel> getCifModels() throws IOException {
    return this.structureModels;
  }

  /**
   * @return List<Model> list of structure models
   * @throws IOException
   */
  public List<Model> getModels() throws IOException {

    List<Model> models = new ArrayList<Model>();
    for (CifModel cifModel : this.structureModels) {
      CifModel pdbModelFiltered = (CifModel) cifModel.filteredNewInstance(MoleculeType.RNA);
      HashMap<String, List<Integer>> missingResidues = new HashMap<>();
      for (PdbChain chain : pdbModelFiltered.chains()) {
        int position = 0;
        for (PdbResidue residue : chain.residues()) {
          if (residue.isMissing()) {
            List<Integer> residuePos =
                missingResidues.getOrDefault(residue.chainIdentifier(), new ArrayList<>());
            residuePos.add(position);
            missingResidues.put(residue.chainIdentifier(), residuePos);
          }
          position++;
        }
      }
      List<Chain> chains = new ArrayList<Chain>();
      for (PdbChain chain : pdbModelFiltered.chains()) {

        chains.add(
            ImmutableChain.builder()
                .name(chain.identifier())
                .sequence(chain.sequence())
                .residuesWithoutAtoms(
                    missingResidues.getOrDefault(chain.identifier(), new ArrayList<>()))
                .build());
      }
      if (chains.size() > 0) {
        models.add(
            ImmutableModel.builder()
                .name(String.valueOf(pdbModelFiltered.modelNumber()))
                .chains(chains)
                .build());
      }
    }
    return models;
  }

  public String filterParseCif(List<Selection> selections) throws IOException {
    CifModel pdbModelFiltered =
        (CifModel)
            structureModels
                .get(Integer.parseInt(selections.get(0).modelName()) - 1)
                .filteredNewInstance(MoleculeType.RNA);

    List<PdbAtomLine> resultAtoms = new ArrayList<PdbAtomLine>();
    for (Selection selection : selections) {

      if (selection.chains().isEmpty()) {
        return pdbModelFiltered.toCif();
      }

      for (PdbChain chain : pdbModelFiltered.chains()) {
        for (SelectionChain selectionChain : selection.chains()) {
          if (chain.identifier().equals(selectionChain.name())) {
            int residue_pos = 0;
            for (PdbResidue residue : chain.residues()) {
              if (residue_pos >= selectionChain.nucleotideRange().fromInclusive()
                  && residue_pos <= selectionChain.nucleotideRange().toInclusive()) {
                resultAtoms.addAll(residue.atoms());
              }
              residue_pos++;
            }
          }
        }
      }
    }
    return ImmutableDefaultCifModel.of(
            pdbModelFiltered.header(),
            pdbModelFiltered.experimentalData(),
            pdbModelFiltered.resolution(),
            pdbModelFiltered.modelNumber(),
            resultAtoms,
            pdbModelFiltered.modifiedResidues(),
            pdbModelFiltered.missingResidues(),
            pdbModelFiltered.title(),
            pdbModelFiltered.chainTerminatedAfter(),
            pdbModelFiltered.basePairs())
        .toCif();
  }
}
