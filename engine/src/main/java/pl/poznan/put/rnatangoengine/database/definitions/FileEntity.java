package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "files")
public class FileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private String filename;

  @Lob private byte[] content;

  public FileEntity() {}

  public FileEntity(String filename, byte[] content) {
    this.filename = filename;
    this.content = content;
  }

  public UUID getHashId() {
    return hashId;
  }

  public byte[] getContent() {
    return content;
  }

  public String getFilename() {
    return filename;
  }
}
