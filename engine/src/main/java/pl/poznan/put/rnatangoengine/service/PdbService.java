package pl.poznan.put.rnatangoengine.service;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.business.Structure;
import pl.poznan.put.rnatangoengine.dto.ImmutableStructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.StructureFileOutput;
import pl.poznan.put.rnatangoengine.dto.StructurePdbInput;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;

@Service
public class PdbService {
  @Autowired StructureProcessingService structureProcessingService;

  public StructureFileOutput pdb(StructurePdbInput structurePdbInput) {

    if (structurePdbInput.name().length() == 4) {
      try {
        URI uri = new URI("https://files.rcsb.org/download/" + structurePdbInput.name() + ".cif");
        URL url = uri.toURL();
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (con.getResponseCode() != 200) {
          throw new ResponseStatusException(HttpStatus.NOT_FOUND, "structure does not exist");
        }
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.flush();
        out.close();
        Structure structure =
            structureProcessingService.process(out.toString(), structurePdbInput.name() + ".cif");
        return ImmutableStructureFileOutput.builder().addAllModels(structure.getModels()).build();
      } catch (Exception e) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "structure does not exist");
      }
    } else {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "structure does not exist");
    }
  }
}
