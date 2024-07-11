package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "commonChainSequence")
public class CommonChainSequenceEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private String chain;

  private String sequence;

  public CommonChainSequenceEntity(String chain, String sequence) {
    this.chain = chain;
    this.sequence = sequence;
  }

  public String getSequence() {
    return this.sequence;
  }

  public String getChain() {
    return this.chain;
  }
}
