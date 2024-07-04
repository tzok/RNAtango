package pl.poznan.put.rnatangoengine.database.converters;

import pl.poznan.put.rnatangoengine.dto.Angle;

public class ExportAngleNameToAngle {

  // public ExportAngleNameToAngle() {}

  public static String parse(Angle angle) {
    switch (angle) {
      case ALPHA:
        return "alpha";
      case BETA:
        return "beta";
      case GAMMA:
        return "gamma";
      case DELTA:
        return "delta";
      case EPSILON:
        return "epsilon";
      case ZETA:
        return "zeta";
      case CHI:
        return "chi";
      case ETA:
        return "eta";
      case THETA:
        return "theta";
      case ETA_PRIM:
        return "eta-prim";
      case THETA_PRIM:
        return "theta-prim";
    }
    return null;
  }

  public static Angle parse(String exportName) {
    switch (exportName) {
      case "alpha":
        return Angle.ALPHA;
      case "beta":
        return Angle.BETA;
      case "gamma":
        return Angle.GAMMA;
      case "delta":
        return Angle.DELTA;
      case "epsilon":
        return Angle.EPSILON;
      case "zeta":
        return Angle.ZETA;
      case "chi":
        return Angle.CHI;
      case "eta":
        return Angle.ETA;
      case "theta":
        return Angle.THETA;
      case "eta-prim":
        return Angle.ETA_PRIM;
      case "theta-prim":
        return Angle.THETA_PRIM;
    }
    return null;
  }
}
