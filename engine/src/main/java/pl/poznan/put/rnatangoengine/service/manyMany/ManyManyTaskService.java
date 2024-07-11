package pl.poznan.put.rnatangoengine.service.manyMany;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.poznan.put.rnatangoengine.database.definitions.CommonChainSequenceEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.CommonChainSequenceRepository;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.service.StructureModelService;

@Service
public class ManyManyTaskService {
  @Autowired ManyManyRepository manyManyRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired CommonChainSequenceRepository commonChainSequenceRepository;
  @Autowired StructureModelService structureModelService;
  private static Logger logger = LogManager.getLogger(ManyManyTaskService.class);

  // public OneManyResultEntity submitTask(
  //     UUID oneManyEntityHashId, List<Angle> angles, Double threshold) throws Exception {
  //   OneManyResultEntity _oneManyResultEntity =
  // oneManyRepository.getByHashId(oneManyEntityHashId);
  //   if (_oneManyResultEntity.equals(null)) {
  //     throw new Exception("task does not exist");
  //   }
  //   _oneManyResultEntity.setThreshold(threshold);
  //   _oneManyResultEntity.setAnglesToAnalyze(angles);
  //   _oneManyResultEntity.setStatus(Status.WAITING);
  //   _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);

  //   try {
  //     queueService.sendOneMany(_oneManyResultEntity.getHashId());
  //   } catch (Exception e) {
  //     _oneManyResultEntity.setStatus(Status.FAILED);
  //     _oneManyResultEntity.setUserErrorLog("Error during setting task");
  //     _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);

  //     e.printStackTrace();
  //   }
  //   return _oneManyResultEntity;
  // }

  public ManyManyResultEntity setTask(StructureModelEntity model) {

    ManyManyResultEntity _manyManyResultEntity =
        manyManyRepository.saveAndFlush(new ManyManyResultEntity());

    _manyManyResultEntity.addModel(model);

    return manyManyRepository.saveAndFlush(_manyManyResultEntity);
  }

  private class InnerCommonSequenceInput {
    String chainI;
    List<List<String>> sequencesI;

    InnerCommonSequenceInput(String chainI, List<List<String>> sequencesI) {
      this.chainI = chainI;
      this.sequencesI = sequencesI;
    }
  }

  private class InnerCommonSequenceOutput {
    String chain;
    String commonSequence;
  }

  public void calculateCommonChainSequeces(ManyManyResultEntity manyManyResultEntity) {
    HashMap<String, List<List<String>>> sequenceCollection = new HashMap<>();
    for (StructureModelEntity model : manyManyResultEntity.getModels()) {
      HashMap<String, List<String>> localSequences = new HashMap<>();
      for (SelectionChainEntity sequence : model.getSelection().getSelectionChains()) {
        List<String> sequences = localSequences.getOrDefault(sequence.getName(), new ArrayList<>());
        sequences.add(sequence.getSequence());
        localSequences.put(sequence.getName(), sequences);
      }
      for (String chain : localSequences.keySet()) {
        List<List<String>> sequences = sequenceCollection.getOrDefault(chain, new ArrayList<>());
        sequences.add(localSequences.get(chain));
        sequenceCollection.put(chain, sequences);
      }
    }
    List<InnerCommonSequenceInput> programInput = new ArrayList<>();
    for (String chain : sequenceCollection.keySet()) {
      if (sequenceCollection.get(chain).size() != manyManyResultEntity.getModels().size()) {
        sequenceCollection.remove(chain);
      } else {
        programInput.add(new InnerCommonSequenceInput(chain, sequenceCollection.get(chain)));
      }
    }
    if (sequenceCollection.keySet().isEmpty()) {
      manyManyResultEntity.setCommonSequences(new ArrayList<>());
      manyManyRepository.saveAndFlush(manyManyResultEntity);
    } else {
      Gson gson = new Gson();
      try {
        ProcessBuilder processBuilder =
            new ProcessBuilder("/opt/rnatango/lcs-rnatango-exe", gson.toJson(programInput));

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        String outputString = "";
        while ((line = reader.readLine()) != null) {

          outputString = outputString.concat(line);
        }
        process.waitFor();

        List<CommonChainSequenceEntity> commonChainSequences = new ArrayList<>();
        String innerJsonArray = gson.fromJson(outputString, String.class);

        List<InnerCommonSequenceOutput> result =
            gson.fromJson(
                innerJsonArray, new TypeToken<List<InnerCommonSequenceOutput>>() {}.getType());
        for (InnerCommonSequenceOutput output : result) {
          commonChainSequences.add(
              new CommonChainSequenceEntity(output.chain, output.commonSequence));
        }
        commonChainSequenceRepository.saveAllAndFlush(commonChainSequences);
        manyManyResultEntity.setCommonSequences(commonChainSequences);
        manyManyRepository.saveAndFlush(manyManyResultEntity);

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  // public OneManyResultEntity addModel(byte[] content, String filename, UUID oneManyEntityHashId)
  //     throws Exception, ModelTargetMatchingException {
  //   OneManyResultEntity _oneManyResultEntity =
  // oneManyRepository.getByHashId(oneManyEntityHashId);
  //   if (_oneManyResultEntity == null) {
  //     throw new Exception("task does not exist");
  //   }
  //   StructureModelEntity model =
  //       structureModelService.createModelFromBytes(
  //           content, filename, _oneManyResultEntity.getChain());

  //   _oneManyResultEntity.addModel(
  //       structureModelService.applyModelTargetCommonSequence(
  //           model, _oneManyResultEntity.getTargetEntity().getSourceSequence()));
  //   _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);
  //   return oneManyUtils.applyCommonSubsequenceToTarget(_oneManyResultEntity);
  // }

  // public OneManyResultEntity removeModel(UUID modelhashId, UUID oneManyEntityHashId)
  //     throws Exception {
  //   OneManyResultEntity _oneManyResultEntity =
  // oneManyRepository.getByHashId(oneManyEntityHashId);
  //   if (_oneManyResultEntity == null) {
  //     throw new Exception("task does not exist");
  //   }
  //   _oneManyResultEntity.removeModel(structureModelRepository.getByHashId(modelhashId));
  //   _oneManyResultEntity = oneManyRepository.saveAndFlush(_oneManyResultEntity);
  //   structureModelRepository.deleteByHashId(modelhashId);
  //   return oneManyUtils.applyCommonSubsequenceToTarget(_oneManyResultEntity);
  // }
}
