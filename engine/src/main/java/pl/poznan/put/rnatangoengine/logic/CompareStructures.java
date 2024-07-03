package pl.poznan.put.rnatangoengine.logic;

import java.util.List;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.dto.IndexPair;
import pl.poznan.put.rnatangoengine.dto.StructureChainSequence;
import pl.poznan.put.rnatangoengine.dto.StructureComparingResult;

@Service
public class CompareStructures {

  class LCSResult {
    public int targetBeginIndex;
    public int targetEndIndex;
    public int modelBeginIndex;
    public int modelEndIndex;
    public String sequence;

    public LCSResult() {}
  }

  public LCSResult longestCommonSubstring(String targetSequence, String modelSequence) {

    int n = targetSequence.length();
    int m = modelSequence.length();
    int[][] op = new int[n + 1][m + 1];
    for (int i = 0; i < n + 1; i++) {
      op[i][0] = 0;
    }
    for (int i = 0; i < m + 1; i++) {
      op[0][i] = 0;
    }
    int length = 0;
    int targetEndIndex = 0;
    int modelEndIndex = 0;
    for (int i = 1; i < n + 1; i++) {
      for (int j = 1; j < m + 1; j++) {
        if (targetSequence.charAt(i - 1) == modelSequence.charAt(j - 1)) {
          op[i][j] = op[i - 1][j - 1] + 1;
          if (op[i][j] > length) {
            length = op[i][j];
            targetEndIndex = i - 1;
            modelEndIndex = j - 1;
          }
        }
      }
    }
    LCSResult lcsResult = new LCSResult();
    lcsResult.sequence = targetSequence.substring(targetEndIndex - length + 1, targetEndIndex + 1);
    lcsResult.targetBeginIndex = targetEndIndex - length + 1;
    lcsResult.targetEndIndex = targetEndIndex;
    lcsResult.modelBeginIndex = modelEndIndex - length + 1;
    lcsResult.modelEndIndex = modelEndIndex;
    return lcsResult;
  }

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

    structureComparingResult.setModelFromInclusiveRelative(startingIndexModel);
    structureComparingResult.setModelToInclusiveRelative(
        startingIndexModel + structureComparingResult.getSequence().length() - 1);
    return structureComparingResult;
  }

  public StructureComparingResult compareTargetAndModelSequences(
      String targetSequence, List<StructureChainSequence> structureChainSequence) throws Exception {
    StructureComparingResult structureComparingResult = new StructureComparingResult();
    LCSResult lcsResult;
    for (StructureChainSequence sequence : structureChainSequence) {
      lcsResult = longestCommonSubstring(targetSequence, sequence.getSequence());
      if (lcsResult.sequence.length() > structureComparingResult.getLength()) {
        structureComparingResult.setSequence(lcsResult.sequence);
        structureComparingResult.setModel(sequence);
        structureComparingResult.setModelToInclusiveRelative(lcsResult.modelEndIndex);
        structureComparingResult.setModelFromInclusiveRelative(lcsResult.modelBeginIndex);

        structureComparingResult.setTargetToInclusiveRelative(lcsResult.targetEndIndex);
        structureComparingResult.setTargetFromInclusiveRelative(lcsResult.targetBeginIndex);
      }
    }
    if (structureComparingResult.getModel() == null) {
      throw new Exception("Target no match model");
    }
    return structureComparingResult;
    // return findIndexesOfBestMatch(targetSequence, structureComparingResult);
  }
}
