package pl.poznan.put.rnatangoengine.logic;

import java.util.ArrayList;
import java.util.List;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.rnatangoengine.dto.Chain;
import pl.poznan.put.rnatangoengine.dto.ImmutableChain;
import pl.poznan.put.rnatangoengine.dto.ImmutableModel;
import pl.poznan.put.rnatangoengine.dto.Model;

public class Structure {
  String structureFileContent;

  public Structure(String structureFileContent) {
    this.structureFileContent = structureFileContent;
  }

  public List<Model> getModels() {
    List<Model> models = new ArrayList<Model>();
    final PdbParser parser = new PdbParser();
    final List<PdbModel> fileModels = parser.parse(this.structureFileContent);

    for (PdbModel pdbModel : fileModels) {
      List<Chain> chains = new ArrayList<Chain>();
      for (PdbChain chain : pdbModel.chains()) {
        chains.add(
            ImmutableChain.builder().name(chain.identifier()).sequence(chain.sequence()).build());
      }
      models.add(
          ImmutableModel.builder()
              .name(String.valueOf(pdbModel.modelNumber()))
              .chains(chains)
              .build());
    }
    return models;
  }
}
