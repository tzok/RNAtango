package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import pl.poznan.put.pdb.analysis.CifModel;
import pl.poznan.put.rnatangoengine.database.converters.CifModelListConverter;
import pl.poznan.put.rnatangoengine.dto.File;

@Entity
@Table(name = "files")
public class FileEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private String filename;

  private String content;

  @Convert(converter = CifModelListConverter.class)
  @Column(name = "cifModels", nullable = false)
  private List<CifModel> cifModels;

  public FileEntity(File file, List<CifModel> cifModels) {
    this.filename = file.filename();
    this.content = file.content();
    this.cifModels = cifModels;
  }

  public FileEntity(String filename, String content, List<CifModel> cifModels) {
    this.filename = filename;
    this.content = content;
    this.cifModels = cifModels;
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
