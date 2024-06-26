package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.rnatangoengine.database.converters.ExportAngleNameToAngle;
import pl.poznan.put.rnatangoengine.dto.Angle;
import pl.poznan.put.rnatangoengine.dto.AngleValue;
import pl.poznan.put.rnatangoengine.dto.ImmutableAngleValue;
import pl.poznan.put.rnatangoengine.dto.ImmutableResidue;
import pl.poznan.put.rnatangoengine.dto.Residue;

@Entity
@Table(name = "residueTorsionAngle")
public class ResidueTorsionAngleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  private String name;

  private int number;

  private String icode;

  private Double alpha;
  private Double beta;
  private Double gamma;
  private Double delta;
  private Double epsilon;
  private Double zeta;
  private Double chi;
  private Double eta;
  private Double theta;
  private Double eta_prim;
  private Double theta_prim;

  @ManyToMany(mappedBy = "residuesTorsionAngleEntities", fetch = FetchType.EAGER)
  List<ChainTorsionAngleEntity> chainsTorsionAngleEntites;

  public ResidueTorsionAngleEntity() {}

  public ResidueTorsionAngleEntity(String name, int number, String icode) {
    this.name = name;
    this.number = number;
    this.icode = icode;
  }

  public List<AngleValue> getAllAngleValues() {
    ExportAngleNameToAngle exportAngleNameToAngle = new ExportAngleNameToAngle();

    List<AngleValue> angleValues =
        MoleculeType.RNA.allAngleTypes().stream()
            .map(
                (angleName) -> {
                  if (exportAngleNameToAngle.parse(angleName.exportName()) != null) {
                    return ImmutableAngleValue.builder()
                        .value(getAngle(exportAngleNameToAngle.parse(angleName.exportName())))
                        .angle(exportAngleNameToAngle.parse(angleName.exportName()))
                        .build();
                  } else {
                    return null;
                  }
                })
            .collect(Collectors.toList());
    angleValues.removeAll(Collections.singleton(null));
    return angleValues;
  }

  public Residue getConvertedToResidueImmutable() {
    return ImmutableResidue.builder()
        .icode(this.icode)
        .name(this.name)
        .number(this.number)
        .addAllTorsionAngles(this.getAllAngleValues())
        .build();
  }

  public String getName() {
    return this.name;
  }

  public int getNumber() {
    return this.number;
  }

  public Optional<String> getIcode() {
    return this.icode.describeConstable();
  }

  public Double getAngle(Angle angle) {
    if (angle == null) {
      return null;
    }
    switch (angle) {
      case ALPHA:
        return this.alpha;
      case BETA:
        return this.beta;
      case GAMMA:
        return this.gamma;
      case DELTA:
        return this.delta;
      case EPSILON:
        return this.epsilon;
      case ZETA:
        return this.zeta;
      case CHI:
        return this.chi;
      case ETA:
        return this.eta;
      case THETA:
        return this.theta;
      case ETA_PRIM:
        return this.eta_prim;
      case THETA_PRIM:
        return this.theta_prim;
      default:
        return null;
    }
  }

  public void setAngle(Angle angle, Double value) {
    if (angle == null) {
      return;
    }
    switch (angle) {
      case ALPHA:
        this.alpha = value;
        break;
      case BETA:
        this.beta = value;
        break;
      case GAMMA:
        this.gamma = value;
        break;
      case DELTA:
        this.delta = value;
        break;
      case EPSILON:
        this.epsilon = value;
        break;
      case ZETA:
        this.zeta = value;
        break;
      case CHI:
        this.chi = value;
        break;
      case ETA:
        this.eta = value;
        break;
      case THETA:
        this.theta = value;
        break;
      case ETA_PRIM:
        this.eta_prim = value;
        break;
      case THETA_PRIM:
        this.theta_prim = value;
        break;
      default:
        break;
    }
  }
}
