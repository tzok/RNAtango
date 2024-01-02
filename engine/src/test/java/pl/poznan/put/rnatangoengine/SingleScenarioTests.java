package pl.poznan.put.rnatangoengine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionEntity;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;
import pl.poznan.put.rnatangoengine.dto.ImmutableNucleotideRange;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelection;
import pl.poznan.put.rnatangoengine.dto.ImmutableSelectionChain;
import pl.poznan.put.rnatangoengine.logic.singleProcessing.SingleProcessing;

@SpringBootTest
@AutoConfigureMockMvc
public class SingleScenarioTests {
  @Autowired SingleResultRepository singleRepository;
  @Autowired SingleProcessing singleProcessing;

  @Test
  public void givenStructure1ffk_whenProcess_thenCorrectResult() throws Exception {
    List<SelectionEntity> selections = new ArrayList<>();
    selections.add(
        new SelectionEntity(
            ImmutableSelection.builder()
                .modelName("1")
                .addChains(
                    ImmutableSelectionChain.builder()
                        .name("0")
                        .nucleotideRange(
                            ImmutableNucleotideRange.builder()
                                .fromInclusive(10)
                                .toInclusive(15)
                                .build())
                        .build())
                .build()));

    SingleResultEntity _singleResultEntity =
        singleRepository.saveAndFlush(new SingleResultEntity(selections, "1ffk"));
    singleProcessing.startTask(_singleResultEntity.getHashId());
    SingleResultEntity result = singleRepository.getByHashId(_singleResultEntity.getHashId());
    assertEquals(result.getErrorLog(), null);
    assertEquals(result.getChainTorsionAngles().size(), 1);
    assertEquals(result.getChainTorsionAngles().get(0).getResiduesTorsionAngles().size(), 6);
  }
}
