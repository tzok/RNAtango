package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import pl.poznan.put.rnatangoengine.dto.File;
import pl.poznan.put.rnatangoengine.dto.Model;

@Entity
@Table(name = "files")
public class FileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private String filename;

  private String content;

  List<Model> models;

  public FileEntity(File file, List<Model> models) {
    this.filename = file.filename();
    this.content = file.content();
    this.models = models;
  }

  public FileEntity(String filename, String content, List<Model> models) {
    this.filename = filename;
    this.content = content;
    this.models = models;
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
