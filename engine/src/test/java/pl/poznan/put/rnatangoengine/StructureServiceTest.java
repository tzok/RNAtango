package pl.poznan.put.rnatangoengine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import pl.poznan.put.rnatangoengine.dto.Molecule;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;

@SpringBootTest
@AutoConfigureMockMvc
public class StructureServiceTest {
  @Autowired StructureProcessingService structureProcessingService;

  @Test
  public void givenStructure1ffk_whenAsked_thenXray() throws Exception {

    assertEquals(structureProcessingService.getStructureMolecule("1ffk"), Molecule.XRAY);
  }

  @Test
  public void givenStructure8COO_whenAsked_thenXray() throws Exception {

    assertEquals(structureProcessingService.getStructureMolecule("8COO"), Molecule.NMR);
  }

  @Test
  public void givenStructure8JIV_whenAsked_thenXray() throws Exception {

    assertEquals(structureProcessingService.getStructureMolecule("8JIV"), Molecule.EM);
  }
}
