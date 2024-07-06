package pl.poznan.put.rnatangoengine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.poznan.put.rnatangoengine.dto.ManyManyInput;
import pl.poznan.put.rnatangoengine.dto.ManyManyOutput;
import pl.poznan.put.rnatangoengine.dto.StatusResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;
import pl.poznan.put.rnatangoengine.service.manyMany.ManyManyService;

@RestController
public class ManyManyController {
  private final ManyManyService manyManyService;

  @Autowired
  public ManyManyController(final ManyManyService manyManyService) {
    this.manyManyService = manyManyService;
  }

  @PostMapping("/many-many")
  public ResponseEntity<TaskIdResponse> manyMany(@RequestBody ManyManyInput input) {
    return new ResponseEntity<>(manyManyService.manyMany(input), HttpStatus.ACCEPTED);
  }

  @GetMapping("/many-many/{taskId}")
  public StatusResponse manyManyStatus(@PathVariable String taskId) {
    return manyManyService.manyManyStatus(taskId);
  }

  @GetMapping("/many-many/{taskId}/result")
  public ManyManyOutput manyManyResult(@PathVariable String taskId) {
    return manyManyService.manyManyResult(taskId);
  }
}
