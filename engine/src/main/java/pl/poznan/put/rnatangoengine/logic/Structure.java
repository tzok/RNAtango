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
import pl.poznan.put.rnatangoengine.database.service.DefaultFileService;
import pl.poznan.put.rnatangoengine.dto.Chain;
import pl.poznan.put.rnatangoengine.dto.ImmutableChain;
import pl.poznan.put.rnatangoengine.dto.ImmutableModel;
import pl.poznan.put.rnatangoengine.dto.Model;
import pl.poznan.put.rnatangoengine.dto.Selection;

public class Structure {
  @Autowired DefaultFileService defaultFileService;

  String structureFileContent;

  /**
   * @param structureCode
   * @throws Exception
   */
  public Structure(String structureCode) throws Exception {
    if (structureCode.length() == 4) {
      getFromRCSB(structureCode);
    } else if (structureCode.startsWith("example_")) {
      getFromLocalExamples(structureCode.replace("example_", ""));
    } else if (structureCode.length() > 4) {
      structureFileContent =
          defaultFileService.getByHashId(UUID.fromString(structureCode)).getContent();
    } else {

      throw new FileNotFoundException("Could not find structure file");
    }
  }

  /**
   * @param structureFileContent
   * @param filename
   * @throws IOException
   */
  public Structure(String structureFileContent, String filename) throws IOException {
    List<String> filenameElements = List.of(filename.split("."));
    if (filenameElements.size() > 0) {
      if (filenameElements.get(filenameElements.size() - 1).toLowerCase().equals("cif")) {
        this.structureFileContent = structureFileContent;
        return;
      }
      if (filenameElements.get(filenameElements.size() - 1).toLowerCase().equals("pdb")) {
        final PdbParser parser = new PdbParser();
        final List<PdbModel> fileModels = parser.parse(this.structureFileContent);
        this.structureFileContent = fileModels.get(0).toCif();
        return;
      }
      throw new IOException("Cannot parse structure file");
    }
  }

  /**
   * @param exampleName
   */
  private void getFromLocalExamples(String exampleName) throws IllegalArgumentException {

    ClassLoader classLoader = getClass().getClassLoader();
    InputStream inputStream = classLoader.getResourceAsStream("exampleStructure/" + exampleName);

    // the stream holding the file content
    if (inputStream == null) {
      throw new IllegalArgumentException("example not found! " + exampleName);
    } else {
      InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      BufferedReader reader = new BufferedReader(streamReader);
      structureFileContent = reader.lines().collect(Collectors.joining());
    }
  }

  /**
   * @param rcsbName
   * @throws Exception
   */
  private void getFromRCSB(String rcsbName) throws Exception {
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
      structureFileContent = out.toString();
    } catch (Exception e) {
      throw new Exception("Could not get structure from rcsb");
    }
  }

  /**
   * @return
   * @throws IOException
   */
  public List<Model> getModels() throws IOException {
    List<Model> models = new ArrayList<Model>();
    final CifParser parser = new CifParser();
    final List<CifModel> fileModels = parser.parse(this.structureFileContent);

    for (CifModel cifModel : fileModels) {
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
   * @param selections
   * @throws IOException
   */
  public void filter(List<Selection> selections) throws IOException {

    final CifParser parser = new CifParser();
    final List<CifModel> fileModels = parser.parse(this.structureFileContent);
    List<CifModel> resultStructure = new ArrayList<>();

    for (Selection selection : selections) {
      CifModel pdbModelFiltered =
          (CifModel)
              fileModels
                  .get(Integer.parseInt(selection.modelName()))
                  .filteredNewInstance(MoleculeType.RNA);

      List<PdbAtomLine> resultAtoms = new ArrayList<PdbAtomLine>();

      for (PdbChain chain : pdbModelFiltered.chains()) {
        if (chain.identifier().equals(selection.chainName())) {
          for (PdbResidue residue : chain.residues()) {
            if (residue.residueNumber() >= selection.nucleotideRange().fromInclusive()
                && residue.residueNumber() <= selection.nucleotideRange().toInclusive()) {
              resultAtoms.addAll(residue.atoms());
            }
          }
        }
      }
      resultStructure.add(
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
              pdbModelFiltered.basePairs()));
    }
    // FIXME: how to parse to all model cif
    this.structureFileContent = resultStructure.get(0).toCif();
  }

  public String getStructureFileContent() {
    return this.structureFileContent;
  }
}
