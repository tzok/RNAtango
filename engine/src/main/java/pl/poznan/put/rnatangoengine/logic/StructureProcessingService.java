package pl.poznan.put.rnatangoengine.logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.pdb.analysis.CifModel;
import pl.poznan.put.pdb.analysis.CifParser;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;

@Configurable
@Service
public class StructureProcessingService {
  @Autowired private FileRepository fileRepository;

  enum Format {
    Cif,
    Pdb
  }

  public StructureProcessingService() {}

  /**
   * @param structureCode
   * @throws Exception
   */
  public Structure process(String structureCode) throws IOException {
    String structureFileContent;

    if (structureCode.length() == 4) {
      structureFileContent = getFromRCSB(structureCode);
      return parseStructureFile(structureFileContent, Format.Cif);
    } else if (structureCode.startsWith("example_")) {
      structureFileContent = getFromLocalExamples(structureCode.replace("example_", ""));
      return parseStructureFile(structureFileContent, Format.Cif); // FIXME: to check
    } else if (structureCode.length() > 4) {
      FileEntity file = fileRepository.getByHashId(UUID.fromString(structureCode));
      if (file == null) {
        throw new ResponseStatusException(HttpStatus.GONE, "Structure file could not be found");
      }
      structureFileContent = new String(file.getContent(), StandardCharsets.UTF_8);
      String[] filenameElements = file.getFilename().split("\\.");
      fileRepository.deleteByHashId(UUID.fromString(structureCode));
      if (filenameElements[filenameElements.length - 1].toLowerCase().equals("cif")) {
        return parseStructureFile(structureFileContent, Format.Cif);
      }
      if (filenameElements[filenameElements.length - 1].toLowerCase().equals("pdb")) {
        return parseStructureFile(structureFileContent, Format.Pdb);
      }
    }
    throw new FileNotFoundException("Structure file could not be found");
  }

  /**
   * @param structureFileContent
   * @param filename
   * @throws IOException
   */
  public Structure process(String structureFileContent, String filename) throws IOException {
    String[] filenameElements = filename.split("\\.");
    if (filenameElements.length > 0) {
      if (filenameElements[filenameElements.length - 1].toLowerCase().equals("cif")) {
        return parseStructureFile(structureFileContent, Format.Cif);
      }
      if (filenameElements[filenameElements.length - 1].toLowerCase().equals("pdb")) {
        return parseStructureFile(structureFileContent, Format.Pdb);
      }
    }

    throw new IOException("Cannot parse structure file");
  }

  private Structure parseStructureFile(String structureFileContent, Format format)
      throws IOException {
    List<CifModel> structureModels = new ArrayList<CifModel>();
    switch (format) {
      case Cif:
        final CifParser parser = new CifParser();
        structureModels = parser.parse(structureFileContent);
        break;

      case Pdb:
        final PdbParser pdbParser = new PdbParser();
        final CifParser cifParser = new CifParser();
        for (PdbModel pdbModel : pdbParser.parse(structureFileContent)) {
          structureModels.addAll(cifParser.parse(pdbModel.toCif()));
        }
        break;
      default:
        throw new IOException("Cannot parse structure file");
    }
    return new Structure(structureModels);
  }

  /**
   * @param exampleName
   */
  private String getFromLocalExamples(String exampleName) throws IllegalArgumentException {

    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("exampleStructure/" + exampleName);

    if (inputStream == null) {
      throw new IllegalArgumentException("example not found! " + exampleName);
    } else {
      InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader reader = new BufferedReader(streamReader);
      return reader.lines().collect(Collectors.joining());
    }
  }

  /**
   * @param rcsbName - pdb id from Protein Data Bank
   * @throws Exception
   */
  private String getFromRCSB(String rcsbName) throws IOException {
    try {
      URI uri = new URI("https://files.rcsb.org/download/" + rcsbName + ".cif");
      URL url = uri.toURL();
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setRequestMethod("GET");
      if (con.getResponseCode() != 200) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "structure does not exist");
      }

      BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
      String inputLine;
      StringBuffer response = new StringBuffer();

      while ((inputLine = in.readLine()) != null) {
        response.append(inputLine + "\n");
      }
      in.close();

      return response.toString();
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException("Could not get structure from rcsb");
    }
  }
}
