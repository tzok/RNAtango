package pl.poznan.put.rnatangoengine.dto;

import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
public interface TorsionAngleDifferencesInChain {
  Chain chain();

  List<Residue> residues();

  List<List<Double>> values();

  List<Double> residueMCQs();

  double chainMCQ();

  LCS chainLCS();
}
