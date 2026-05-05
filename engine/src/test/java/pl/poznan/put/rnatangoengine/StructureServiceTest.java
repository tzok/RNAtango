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

    assertEquals(Molecule.XRAY, structureProcessingService.getStructureMolecule("1ffk"));
  }

  @Test
  public void givenStructure8COO_whenAsked_thenXray() throws Exception {

    assertEquals(Molecule.NMR, structureProcessingService.getStructureMolecule("8COO"));
  }

  @Test
  public void givenStructure8JIV_whenAsked_thenXray() throws Exception {

    assertEquals(Molecule.EM, structureProcessingService.getStructureMolecule("8JIV"));
  }
}
