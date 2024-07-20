package pl.poznan.put.rnatangoengine.controller;

import nl.martijndwars.webpush.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.poznan.put.rnatangoengine.WebPushService;
import pl.poznan.put.rnatangoengine.dto.ImmutableTaskIdResponse;
import pl.poznan.put.rnatangoengine.dto.TaskIdResponse;

@RestController
public class WebPushController {

  private WebPushService webPushService;

  @Autowired
  public WebPushController(WebPushService webPushService) {
    this.webPushService = webPushService;
  }

  public String getPublicKey() {
    return webPushService.getPublicKey();
  }

  @PostMapping("/single/{taskId}/subscribe")
  public ResponseEntity<TaskIdResponse> subscribeSingle(
      @RequestBody Subscription subscription, @RequestParam("taskId") String taskId) {
    webPushService.subscribeSingle(subscription, taskId);
    return new ResponseEntity<>(
        ImmutableTaskIdResponse.builder().taskId(taskId).build(), HttpStatus.ACCEPTED);
  }

  @PostMapping("/one-many/{taskId}/subscribe")
  public ResponseEntity<TaskIdResponse> subscribeOneMany(
      @RequestBody Subscription subscription, @RequestParam("taskId") String taskId) {
    webPushService.subscribeOneMany(subscription, taskId);
    return new ResponseEntity<>(
        ImmutableTaskIdResponse.builder().taskId(taskId).build(), HttpStatus.ACCEPTED);
  }

  @PostMapping("/many-many/{taskId}/subscribe")
  public ResponseEntity<TaskIdResponse> subscribeManyMany(
      @RequestBody Subscription subscription, @RequestParam("taskId") String taskId) {
    webPushService.subscribeManyMany(subscription, taskId);
    return new ResponseEntity<>(
        ImmutableTaskIdResponse.builder().taskId(taskId).build(), HttpStatus.ACCEPTED);
  }

  @GetMapping("/single/{taskId}/unsubscribe/{endpoint}")
  public ResponseEntity<TaskIdResponse> unSubscribeSingle(
      @RequestParam("endpoint") String endpoint, @RequestParam("taskId") String taskId) {
    webPushService.unSubscribeSingle(endpoint, taskId);
    return new ResponseEntity<>(
        ImmutableTaskIdResponse.builder().taskId(taskId).build(), HttpStatus.ACCEPTED);
  }

  @GetMapping("/one-many/{taskId}/unsubscribe/{endpoint}")
  public ResponseEntity<TaskIdResponse> unSubscribeOneMany(
      @RequestParam("endpoint") String endpoint, @RequestParam("taskId") String taskId) {
    webPushService.unSubscribeOneMany(endpoint, taskId);
    return new ResponseEntity<>(
        ImmutableTaskIdResponse.builder().taskId(taskId).build(), HttpStatus.ACCEPTED);
  }

  @GetMapping("/many-many/{taskId}/unsubscribe/{endpoint}")
  public ResponseEntity<TaskIdResponse> unSubscribeManyMany(
      @RequestParam("endpoint") String endpoint, @RequestParam("taskId") String taskId) {
    webPushService.unSubscribeManyMany(endpoint, taskId);
    return new ResponseEntity<>(
        ImmutableTaskIdResponse.builder().taskId(taskId).build(), HttpStatus.ACCEPTED);
  }
}
