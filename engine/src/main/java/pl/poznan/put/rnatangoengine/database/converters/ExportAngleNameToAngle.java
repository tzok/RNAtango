package pl.poznan.put.rnatangoengine.database.converters;

import pl.poznan.put.rnatangoengine.dto.Angle;

public class ExportAngleNameToAngle {

  public ExportAngleNameToAngle() {}

  public Angle parse(String exportName) {
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
