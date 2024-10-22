package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import pl.poznan.put.rnatangoengine.dto.ImmutableChain;
import pl.poznan.put.rnatangoengine.dto.ImmutableTorsionAnglesInChain;
import pl.poznan.put.rnatangoengine.dto.TorsionAnglesInChain;

@Entity
@Table(name = "chainTorsionAngle")
public class ChainTorsionAngleEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  protected Long id;

  private String name;

  @Column(length = 5000)
  private String sequence;

  @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  List<ResidueTorsionAngleEntity> residuesTorsionAngleEntities;

  public ChainTorsionAngleEntity() {}

  public ChainTorsionAngleEntity(String name, String sequence) {
    this.name = name;
    this.sequence = sequence.toUpperCase();
    this.residuesTorsionAngleEntities = new ArrayList<>();
  }

  public List<ResidueTorsionAngleEntity> getResiduesTorsionAngles() {
    return this.residuesTorsionAngleEntities;
  }

  public TorsionAnglesInChain getConvertedToTorsionAnglesInChainImmutable() {
    return ImmutableTorsionAnglesInChain.builder()
        .chain(ImmutableChain.builder().name(this.name).sequence(this.sequence).build())
        .addAllResidues(
            this.getResiduesTorsionAngles().stream()
                .map((residue) -> residue.getConvertedToResidueImmutable())
                .collect(Collectors.toList()))
        .build();
  }

  public String getName() {
    return this.name;
  }

  public String getSequence() {
    return this.sequence;
  }

  public Long getId() {
    return id;
  }
}
