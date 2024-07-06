package pl.poznan.put.rnatangoengine.logic.oneManyProcessing;

import org.apache.commons.math3.util.FastMath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.circular.ImmutableAngle;
import pl.poznan.put.comparison.LCS;
import pl.poznan.put.comparison.global.LCSGlobalResult;
import pl.poznan.put.matching.FragmentMatch;
import pl.poznan.put.matching.MatchCollectionDeltaIterator;
import pl.poznan.put.matching.ResidueComparison;
import pl.poznan.put.matching.SelectionMatch;
import pl.poznan.put.matching.StructureSelection;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.rnatangoengine.database.definitions.LCSEntity;
import pl.poznan.put.rnatangoengine.database.repository.LCSRepository;

@Service
public class LcsTaProcessing {
  @Autowired LCSRepository lcsRepository;

  public LCSEntity calculate(
      StructureSelection target, StructureSelection model, Double threshold) {
    LCSEntity lcsEntity = new LCSEntity();
    final LCS lcs = new LCS(MoleculeType.RNA, ImmutableAngle.of(FastMath.toRadians(threshold)));
    final LCSGlobalResult result = (LCSGlobalResult) lcs.compareGlobally(target, model);
    SelectionMatch selectionMatch = result.selectionMatch();
    new MatchCollectionDeltaIterator(selectionMatch);
    int validCount = selectionMatch.getResidueLabels().size();
    int length = target.getResidues().size();
    double coverage = (double) validCount / (double) length * 100.0;
    PdbResidue targetStart =
        ((ResidueComparison)
                ((FragmentMatch) selectionMatch.getFragmentMatches().get(0))
                    .getResidueComparisons()
                    .get(0))
            .target();
    PdbResidue targetEnd =
        ((ResidueComparison)
                ((FragmentMatch) selectionMatch.getFragmentMatches().get(0))
                    .getResidueComparisons()
                    .get(
                        ((FragmentMatch) selectionMatch.getFragmentMatches().get(0))
                                .getResidueComparisons()
                                .size()
                            - 1))
            .target();
    PdbResidue modelStart =
        ((ResidueComparison)
                ((FragmentMatch) selectionMatch.getFragmentMatches().get(0))
                    .getResidueComparisons()
                    .get(0))
            .model();
    PdbResidue modelEnd =
        ((ResidueComparison)
                ((FragmentMatch) selectionMatch.getFragmentMatches().get(0))
                    .getResidueComparisons()
                    .get(
                        ((FragmentMatch) selectionMatch.getFragmentMatches().get(0))
                                .getResidueComparisons()
                                .size()
                            - 1))
            .model();
    // e1.residueNumber()
    lcsEntity.setValidResidues(validCount);
    lcsEntity.setCoveragePercent(coverage);
    lcsEntity.setModelRange(modelStart.residueNumber(), modelEnd.residueNumber());
    lcsEntity.setTargetRange(targetStart.residueNumber(), targetEnd.residueNumber());
    lcsEntity.setMcqValue(result.meanDirection().degrees());
    return lcsRepository.saveAndFlush(lcsEntity);
  }
}
