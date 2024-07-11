package pl.poznan.put.rnatangoengine.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.*;

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

  public Selection convertToSelection() {

    return ImmutableSelection.builder()
        .modelName(this.modelName)
        .addAllChains(
            this.chains.entrySet().stream()
                .flatMap(
                    entry ->
                        entry.getValue().stream()
                            .map(
                                chain ->
                                    ImmutableSelectionChain.builder()
                                        .name(entry.getKey())
                                        .sequence(chain.getSequence())
                                        .nucleotideRange(
                                            ImmutableNucleotideRange.builder()
                                                .fromInclusive(chain.getFrom())
                                                .toInclusive(chain.getTo())
                                                .build())
                                        .build()))
                .collect(Collectors.toList()))
        .build();
  }
}
