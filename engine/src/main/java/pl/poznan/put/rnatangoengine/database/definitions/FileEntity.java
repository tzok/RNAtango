package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.dto.File;

@Entity
@Table(name = "files")
public class FileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private String filename;

  private String content;

  public FileEntity(File file) {
    this.filename = file.filename();
    this.content = file.content();
  }

  public FileEntity(String filename, String content) {
    this.filename = filename;
    this.content = content;
  }

  public UUID getHashId() {
    return hashId;
  }

  public String getContent() {
    return content;
  }

  public String getFilename() {
    return filename;
  }
}
