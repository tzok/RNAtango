package pl.poznan.put.rnatangoengine.logic;

import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
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
import pl.poznan.put.pdb.analysis.ImmutableDefaultCifModel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.repository.FileRepository;
import pl.poznan.put.rnatangoengine.dto.FileFormat;
import pl.poznan.put.rnatangoengine.dto.Molecule;

@Configurable
@Service
public class StructureProcessingService {
  @Autowired private FileRepository fileRepository;

  public StructureProcessingService() {}

  /**
   * @param structureCode
   * @throws Exception
   */
  public Structure process(String structureCode) throws IOException {
    String structureFileContent;
    Structure parsedStructure = null;
    if (structureCode.length() == 4) {
      structureFileContent = getFromRCSB(structureCode);
      parsedStructure = parseStructureFile(structureFileContent, FileFormat.CIF, structureCode);
      parsedStructure.setStructureMolecule(getStructureMolecule(structureCode));
    } else if (structureCode.startsWith("example_")) {
      structureFileContent = getFromLocalExamples(structureCode.replace("example_", ""));
      parsedStructure =
          parseStructureFile(
              structureFileContent,
              FileFormat.CIF,
              structureCode.replace("example_", "")); // FIXME: to check
      parsedStructure.setStructureMolecule(
          getStructureMolecule(structureCode.split(".")[0].replace("example_", "")));

    } else if (structureCode.length() > 4) {
      FileEntity file = fileRepository.getByHashId(UUID.fromString(structureCode));
      if (file == null) {
        throw new ResponseStatusException(HttpStatus.GONE, "Structure file could not be found");
      }
      structureFileContent = new String(file.getContent(), StandardCharsets.UTF_8);
      String[] filenamePathElements = file.getFilename().split("/");
      String[] filenameElements =
          filenamePathElements[filenamePathElements.length - 1].split("\\.");
      fileRepository.deleteByHashId(UUID.fromString(structureCode));
      if (filenameElements[filenameElements.length - 1].toLowerCase().equals("cif")) {
        parsedStructure =
            parseStructureFile(structureFileContent, FileFormat.CIF, filenameElements[0]);
      }
      if (filenameElements[filenameElements.length - 1].toLowerCase().equals("pdb")) {
        parsedStructure =
            parseStructureFile(structureFileContent, FileFormat.PDB, filenameElements[0]);
      }
    }
    if (parsedStructure == null) {
      throw new FileNotFoundException("Structure file could not be found");
    } else {
      return parsedStructure;
    }
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
        return parseStructureFile(structureFileContent, FileFormat.CIF);
      }
      if (filenameElements[filenameElements.length - 1].toLowerCase().equals("pdb")) {
        return parseStructureFile(structureFileContent, FileFormat.PDB);
      }
    }

    throw new IOException("Cannot parse structure file");
  }

  private Structure parseStructureFile(String structureFileContent, FileFormat format)
      throws IOException {
    List<CifModel> structureModels = new ArrayList<CifModel>();
    switch (format) {
      case CIF:
        final CifParser parser = new CifParser();
        structureModels = parser.parse(structureFileContent);
        break;

      case PDB:
        structureModels =
            new PdbParser()
                .parse(structureFileContent).stream()
                    .map(
                        model ->
                            ImmutableDefaultCifModel.of(
                                model.header(),
                                model.experimentalData(),
                                model.resolution(),
                                model.modelNumber(),
                                model.filteredAtoms(MoleculeType.RNA),
                                model.modifiedResidues(),
                                model.filteredMissing(MoleculeType.RNA),
                                model.title(),
                                model.chainTerminatedAfter(),
                                new ArrayList<>()))
                    .collect(Collectors.toList());
        break;
      default:
        throw new IOException("Cannot parse structure file");
    }
    return new Structure(structureModels);
  }

  private Structure parseStructureFile(String structureFileContent, FileFormat format, String name)
      throws IOException {

    Structure structure = parseStructureFile(structureFileContent, format);
    structure.setStructureName(name);
    return structure;
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

  public Molecule getStructureMolecule(String structureCode) {
    String query =
        "{\"query\":\"query structure ($id: String!) {entry(entry_id:$id){exptl{method}}}\",\"variables\":{\"id\":\""
            + structureCode.toUpperCase()
            + "\"},\"operationName\":\"structure\"}";
    try {
      URL url = new URI("https://data.rcsb.org/graphql").toURL();
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setRequestMethod("POST");
      try (OutputStream os = con.getOutputStream()) {
        byte[] input = query.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      try (BufferedReader br =
          new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
        StringBuilder response = new StringBuilder();
        String responseLine = null;
        while ((responseLine = br.readLine()) != null) {
          response.append(responseLine.trim());
        }
        String moleculeString =
            JsonParser.parseString(response.toString())
                .getAsJsonObject()
                .get("data")
                .getAsJsonObject()
                .get("entry")
                .getAsJsonObject()
                .get("exptl")
                .getAsJsonArray()
                .get(0)
                .getAsJsonObject()
                .get("method")
                .getAsString();
        switch (moleculeString) {
          case "ELECTRON MICROSCOPY":
            return Molecule.EM;
          case "X-RAY DIFFRACTION":
            return Molecule.XRAY;
          case "SOLUTION NMR":
            return Molecule.NMR;
          default:
            return Molecule.OTHER;
        }
      }
    } catch (URISyntaxException e) {
      e.printStackTrace();

      return Molecule.NA;
    } catch (IOException e) {
      e.printStackTrace();

      return Molecule.NA;
    } catch (Exception e) {
      e.printStackTrace();
      return Molecule.NA;
    }
  }
}
