package pl.poznan.put.rnatangoengine.service.manyMany;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.definitions.CommonChainSequenceEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.SelectionChainEntity;
import pl.poznan.put.rnatangoengine.database.definitions.StructureModelEntity;
import pl.poznan.put.rnatangoengine.database.repository.CommonChainSequenceRepository;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SelectionRepository;
import pl.poznan.put.rnatangoengine.database.repository.StructureModelRepository;
import pl.poznan.put.rnatangoengine.dto.Angle;
import pl.poznan.put.rnatangoengine.dto.Status;
import pl.poznan.put.rnatangoengine.logic.StructureProcessingService;
import pl.poznan.put.rnatangoengine.logic.manyManyProcessing.ManyManyProcessing;
import pl.poznan.put.rnatangoengine.service.QueueService;
import pl.poznan.put.rnatangoengine.service.StructureModelService;

@Service
public class ManyManyTaskService {
  @Autowired ManyManyRepository manyManyRepository;
  @Autowired SelectionRepository selectionRepository;
  @Autowired StructureProcessingService structureProcessingService;
  @Autowired StructureModelRepository structureModelRepository;
  @Autowired CommonChainSequenceRepository commonChainSequenceRepository;
  @Autowired StructureModelService structureModelService;
  @Autowired QueueService queueService;
  @Autowired ManyManyProcessing processing;

  public ManyManyResultEntity submitTask(
      UUID manyManyEntityHashId, List<Angle> angles, Double threshold, String chain)
      throws Exception {
    ManyManyResultEntity _manyManyResultEntity =
        manyManyRepository.getByHashId(manyManyEntityHashId);
    if (Objects.equals(_manyManyResultEntity, null)) {
      throw new Exception("task does not exist");
    }
    _manyManyResultEntity.setThreshold(threshold);
    _manyManyResultEntity.setAnglesToAnalyze(angles);
    _manyManyResultEntity.setStatus(Status.WAITING);
    _manyManyResultEntity.setChainToAnalyze(chain);
    _manyManyResultEntity.setSequenceToAnalyze(
        _manyManyResultEntity.getCommonSequences().stream()
            .filter(chainE -> chainE.getChain() != chain)
            .map(chainE -> chainE.getSequence())
            .collect(Collectors.toList())
            .get(0));
    _manyManyResultEntity = manyManyRepository.saveAndFlush(_manyManyResultEntity);
    try {
      queueService.sendManyMany(_manyManyResultEntity.getHashId());
    } catch (Exception e) {
      _manyManyResultEntity.setStatus(Status.FAILED);
      _manyManyResultEntity.setUserErrorLog("Error during setting task");
      _manyManyResultEntity = manyManyRepository.saveAndFlush(_manyManyResultEntity);

      e.printStackTrace();
    }
    return _manyManyResultEntity;
  }

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
    List<StructureModelEntity> models = manyManyResultEntity.getModels();
    if (models.size() <= 1) {
      manyManyResultEntity.setCommonSequences(new ArrayList<>());
      manyManyRepository.saveAndFlush(manyManyResultEntity);
      return;
    }
    for (StructureModelEntity model : models) {
      HashMap<String, List<String>> localSequences = new HashMap<>();
      for (SelectionChainEntity sequence : model.getSelection().getSelectionChains()) {
        List<String> sequences = localSequences.getOrDefault(sequence.getName(), new ArrayList<>());
        sequences.add(sequence.getSequence().toUpperCase());
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

  public ManyManyResultEntity addModel(byte[] content, String filename, UUID manyManyEntityHashId)
      throws Exception {
    StructureModelEntity structureModelEntity =
        structureModelService.createInitalModelFromBytes(content, filename);
    ManyManyResultEntity manyManyResultEntity =
        manyManyRepository.getByHashId(manyManyEntityHashId);
    if (Objects.equals(manyManyResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error during processing file");
    }

    manyManyResultEntity.addModel(structureModelEntity);
    manyManyResultEntity = manyManyRepository.saveAndFlush(manyManyResultEntity);
    calculateCommonChainSequeces(manyManyResultEntity);
    return manyManyRepository.getByHashId(manyManyEntityHashId);
  }

  @Transactional
  public ManyManyResultEntity removeModel(UUID modelhashId, UUID manyManyEntityHashId)
      throws Exception {
    ManyManyResultEntity _manyManyResultEntity =
        manyManyRepository.getByHashId(manyManyEntityHashId);
    if (_manyManyResultEntity == null) {
      throw new Exception("task does not exist");
    }
    _manyManyResultEntity.removeModel(structureModelRepository.getByHashId(modelhashId));
    _manyManyResultEntity = manyManyRepository.saveAndFlush(_manyManyResultEntity);
    structureModelRepository.deleteByHashId(modelhashId);
    calculateCommonChainSequeces(_manyManyResultEntity);

    return _manyManyResultEntity;
  }
}
