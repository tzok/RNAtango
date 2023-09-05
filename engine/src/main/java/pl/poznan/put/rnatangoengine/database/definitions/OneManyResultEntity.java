package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "oneManyResults")
public class OneManyResultEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;
}
