package pl.poznan.put.rnatangoengine.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import org.immutables.value.Value;

@Value.Immutable
@Value.Style(privateNoargConstructor = true)
@JsonSerialize(as = ImmutableTorsionAngleDifferences.class)
@JsonDeserialize(as = ImmutableTorsionAngleDifferences.class)
public interface TorsionAngleDifferences {
  String modelHashId();

  String modelName();

  String model();

  // List<TorsionAngleDifferencesInChain> differences();

  double modelMCQ();

  List<Residue> residues();

  // List<List<Double>> values();

  List<Double> residueMCQs();

  // double chainMCQ();

  // LCS chainLCS();
}
