package pl.poznan.put.rnatangoengine.logic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.pdb.PdbAtomLine;
import pl.poznan.put.pdb.analysis.CifModel;
import pl.poznan.put.pdb.analysis.CifParser;
import pl.poznan.put.pdb.analysis.ImmutableDefaultCifModel;
import pl.poznan.put.pdb.analysis.MoleculeType;
import pl.poznan.put.pdb.analysis.PdbChain;
import pl.poznan.put.pdb.analysis.PdbModel;
import pl.poznan.put.pdb.analysis.PdbParser;
import pl.poznan.put.pdb.analysis.PdbResidue;
import pl.poznan.put.rnatangoengine.database.definitions.FileEntity;
import pl.poznan.put.rnatangoengine.database.service.DefaultFileService;
import pl.poznan.put.rnatangoengine.dto.Chain;
import pl.poznan.put.rnatangoengine.dto.ImmutableChain;
import pl.poznan.put.rnatangoengine.dto.ImmutableModel;
import pl.poznan.put.rnatangoengine.dto.Model;
import pl.poznan.put.rnatangoengine.dto.Selection;
import pl.poznan.put.rnatangoengine.dto.SelectionChain;

public class Structure {
  @Autowired private DefaultFileService defaultFileService;
  String structureFileContent;
  List<CifModel> structureModels;
  String structureCode;
  String filename;

  enum Format {
    Cif,
    Pdb
  }

  /**
   * @param structureCode
   * @throws Exception
   */
  public Structure(String structureCode) {
    this.structureModels = new ArrayList<CifModel>();
    this.structureCode = structureCode;
  }

  /**
   * @param structureFileContent
   * @param filename
   * @throws IOException
   */
  public Structure(String structureFileContent, String filename) {
    this.structureModels = new ArrayList<CifModel>();

    this.structureFileContent = structureFileContent;
    this.filename = filename;
  }

  public void process() throws IOException {
    if (!structureCode.isEmpty()) {
      if (structureCode.length() == 4) {
        this.structureFileContent = getFromRCSB(structureCode);
        parseStructureFile(Format.Cif);
      } else if (structureCode.startsWith("example_")) {
        this.structureFileContent = getFromLocalExamples(structureCode.replace("example_", ""));
        parseStructureFile(Format.Cif); // FIXME: to check
      } else if (structureCode.length() > 4) {
        FileEntity file = defaultFileService.getByHashId(UUID.fromString(structureCode));
        this.structureFileContent = file.getContent().toString();
        List<String> filenameElements = List.of(file.getFilename().split("."));
        if (filenameElements.get(filenameElements.size() - 1).toLowerCase().equals("cif")) {
          parseStructureFile(Format.Cif);
        }
        if (filenameElements.get(filenameElements.size() - 1).toLowerCase().equals("pdb")) {
          parseStructureFile(Format.Pdb);
        }
      } else {
        throw new FileNotFoundException("Structure file could not be found");
      }
    }
    if (!this.structureFileContent.isEmpty() && !this.filename.isEmpty()) {
      String[] filenameElements = filename.split("\\.");
      if (filenameElements.length > 0) {
        if (filenameElements[filenameElements.length - 1].toLowerCase().equals("cif")) {
          parseStructureFile(Format.Cif);
          return;
        }
        if (filenameElements[filenameElements.length - 1].toLowerCase().equals("pdb")) {
          parseStructureFile(Format.Pdb);
          return;
        }
      }
      throw new IOException("Cannot parse structure file");
    }
  }

  private void parseStructureFile(Format format) throws IOException {
    switch (format) {
      case Cif:
        final CifParser parser = new CifParser();
        this.structureModels = parser.parse(structureFileContent);
        break;

      case Pdb:
        final PdbParser pdbParser = new PdbParser();
        final CifParser cifParser = new CifParser();
        for (PdbModel pdbModel : pdbParser.parse(structureFileContent)) {
          this.structureModels.addAll(cifParser.parse(pdbModel.toCif()));
        }
        break;
      default:
        throw new IOException("Cannot parse structure file");
    }
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
      con.setRequestMethod("GET");
      if (con.getResponseCode() != 200) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "structure does not exist");
      }
      DataOutputStream out = new DataOutputStream(con.getOutputStream());
      out.flush();
      out.close();
      return out.toString();
    } catch (Exception e) {
      throw new IOException("Could not get structure from rcsb");
    }
  }

  /**
   * @return List<Model> list of structure models
   * @throws IOException
   */
  public List<Model> getModels() throws IOException {

    List<Model> models = new ArrayList<Model>();
    for (CifModel cifModel : this.structureModels) {
      CifModel pdbModelFiltered = (CifModel) cifModel.filteredNewInstance(MoleculeType.RNA);
      List<Chain> chains = new ArrayList<Chain>();
      for (PdbChain chain : pdbModelFiltered.chains()) {
        chains.add(
            ImmutableChain.builder().name(chain.identifier()).sequence(chain.sequence()).build());
      }
      if (chains.size() > 0) {
        models.add(
            ImmutableModel.builder()
                .name(String.valueOf(pdbModelFiltered.modelNumber()))
                .chains(chains)
                .build());
      }
    }
    return models;
  }

  /**
   * @return
   * @throws IOException
   */
  public List<CifModel> getCifModels() throws IOException {
    return this.structureModels;
  }

  /**
   * @param selections
   * @throws IOException
   */
  public String filter(List<Selection> selections) throws IOException {
    CifModel pdbModelFiltered =
        (CifModel) structureModels.get(Integer.parseInt(selections.get(0).modelName()));

    List<PdbAtomLine> resultAtoms = new ArrayList<PdbAtomLine>();
    for (Selection selection : selections) {

      if (selection.chains().isEmpty()) {
        this.structureFileContent = pdbModelFiltered.toCif();
        return this.structureFileContent;
      }

      for (PdbChain chain : pdbModelFiltered.chains()) {
        for (SelectionChain selectionChain : selection.chains()) {

          if (chain.identifier().equals(selectionChain.name())) {
            for (PdbResidue residue : chain.residues()) {
              if (residue.residueNumber() >= selectionChain.nucleotideRange().fromInclusive()
                  && residue.residueNumber() <= selectionChain.nucleotideRange().toInclusive()) {
                resultAtoms.addAll(residue.atoms());
              }
            }
          }
        }
      }
    }
    this.structureFileContent =
        ImmutableDefaultCifModel.of(
                pdbModelFiltered.header(),
                pdbModelFiltered.experimentalData(),
                pdbModelFiltered.resolution(),
                pdbModelFiltered.modelNumber(),
                resultAtoms,
                pdbModelFiltered.modifiedResidues(),
                pdbModelFiltered.missingResidues(),
                pdbModelFiltered.title(),
                pdbModelFiltered.chainTerminatedAfter(),
                pdbModelFiltered.basePairs())
            .toCif();
    return this.structureFileContent;
  }
}
