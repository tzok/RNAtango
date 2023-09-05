package pl.poznan.put.rnatangoengine.service;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.springframework.stereotype.Service;
import pl.poznan.put.pdb.PdbParsingException;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.StructurePdbInput;
import pl.poznan.put.rnatangoengine.logic.Structure;

@Service
public class PdbService {
  public StructureFileOutput pdb(StructurePdbInput structurePdbInput) {
    if (structurePdbInput.name().length() == 4) {
      try {
        URL url = new URL("https://files.rcsb.org/download/" + structurePdbInput.name() + ".cif");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (con.getResponseCode() != 200) {
          throw new PdbParsingException("Structure doesn't exist");
        }
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.flush();
        out.close();
        Structure structure = new Structure(out.toString());
        return ImmutableStructureFileOutput.builder().addAllModels(structure.getModels()).build();
      } catch (Exception e) {
        throw new PdbParsingException("Structure doesn't exist");
      }
    } else {
      throw new PdbParsingException("Structure doesn't exist");
    }
  }
}
