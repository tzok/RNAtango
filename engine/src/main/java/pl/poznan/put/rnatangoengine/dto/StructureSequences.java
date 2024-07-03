package pl.poznan.put.rnatangoengine.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StructureSequences {
  String modelName;

  HashMap<String, List<StructureChainSequence>> chains;

  public StructureSequences(String modelName) {
    this.modelName = modelName;
    this.chains = new HashMap<>();
  }

  public void insertSequence(StructureChainSequence sequence) {
    List<StructureChainSequence> sequences = new ArrayList<>();
    if (this.chains.containsKey(sequence.name)) {
      sequences = chains.get(sequence.name);
    }
    sequences.add(sequence);
    chains.put(sequence.name, sequences);
  }

  public List<StructureChainSequence> getChainSubsequence(String chain) throws Exception {
    if (chains.get(chain).size() == 0) throw new Exception("No sequences was found");
    return chains.get(chain);
  }
}
