package pl.poznan.put.rnatangoengine.logic;

import java.util.List;
import org.apache.commons.text.similarity.LongestCommonSubsequence;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.dto.IndexPair;
import pl.poznan.put.rnatangoengine.dto.StructureChainSequence;
import pl.poznan.put.rnatangoengine.dto.StructureComparingResult;

@Service
public class CompareStructures {

  // find max min and min max from target coverages
  public IndexPair findCommonTargetMapping(List<IndexPair> bestModelTargetMappingIndexes) {
    IndexPair commonTarget = bestModelTargetMappingIndexes.get(0);
    for (IndexPair indexPair : bestModelTargetMappingIndexes) {
      if (commonTarget.toInclusive > indexPair.toInclusive) {
        commonTarget.toInclusive = indexPair.toInclusive;
      }
      if (commonTarget.fromInclusive < indexPair.fromInclusive) {
        commonTarget.fromInclusive = indexPair.fromInclusive;
      }
    }
    return commonTarget;
  }

  public StructureComparingResult findIndexesOfBestMatch(
      String targetSequence, StructureComparingResult structureComparingResult) {
    Integer startingIndexTarget = targetSequence.indexOf(structureComparingResult.getSequence());
    structureComparingResult.setTargetFromInclusiveRelative(startingIndexTarget);
    structureComparingResult.setTargetToInclusiveRelative(
        startingIndexTarget + structureComparingResult.getSequence().length() - 1);

    Integer startingIndexModel =
        structureComparingResult
            .getModel()
            .getSequence()
            .indexOf(structureComparingResult.getSequence());
    structureComparingResult.setModelFromInclusiveRelative(startingIndexTarget);
    structureComparingResult.setModelToInclusiveRelative(
        startingIndexModel + structureComparingResult.getSequence().length() - 1);
    return structureComparingResult;
  }

  public StructureComparingResult compareTargetAndModelSequences(
      String targetSequence, List<StructureChainSequence> structureChainSequence) throws Exception {
    StructureComparingResult structureComparingResult = new StructureComparingResult();
    String subsequence = "";
    for (StructureChainSequence sequence : structureChainSequence) {
      subsequence = getLongestSubsequence(targetSequence, sequence.getSequence());
      if (subsequence.length() > structureComparingResult.getLength()) {
        structureComparingResult.setSequence(subsequence);
        structureComparingResult.setModel(sequence);
      }
    }
    if (structureComparingResult.getLength() == 0) {
      throw new Exception("Target no match model");
    }
    return findIndexesOfBestMatch(targetSequence, structureComparingResult);
  }

  public String getLongestSubsequence(String targetSequence, String modelSequence) {
    LongestCommonSubsequence lcs = new LongestCommonSubsequence();
    return lcs.longestCommonSubsequence(targetSequence, modelSequence).toString();
  }

  public Integer getLongestSubsequenceLength(String targetSequence, String modelSequence) {
    LongestCommonSubsequence lcs = new LongestCommonSubsequence();
    return lcs.apply(targetSequence, modelSequence);
  }
}
