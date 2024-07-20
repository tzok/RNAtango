package pl.poznan.put.rnatangoengine;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.ManyManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.OneManyResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.ScenarioEntities.SingleResultEntity;
import pl.poznan.put.rnatangoengine.database.definitions.WebPushSubscription;
import pl.poznan.put.rnatangoengine.database.repository.ManyManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.OneManyRepository;
import pl.poznan.put.rnatangoengine.database.repository.SingleResultRepository;
import pl.poznan.put.rnatangoengine.database.repository.WebPushRepository;

@Service
public class WebPushService {
  @Autowired WebPushRepository webPushRepository;
  @Autowired SingleResultRepository singleResultRepository;
  @Autowired OneManyRepository oneManyRepository;
  @Autowired ManyManyRepository manyManyRepository;

  @Value("${vapid.public.key}")
  private String publicKey;

  @Value("${vapid.private.key}")
  private String privateKey;

  private PushService pushService;

  @PostConstruct
  private void init() throws GeneralSecurityException {
    Security.addProvider(new BouncyCastleProvider());
    pushService = new PushService(publicKey, privateKey);
  }

  public String getPublicKey() {
    return publicKey;
  }

  public void subscribeSingle(Subscription subscription, String taskId) {
    SingleResultEntity _singleResultEntity =
        singleResultRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_singleResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
    List<WebPushSubscription> subscribers = _singleResultEntity.getSubscibers();
    subscribers.add(
        webPushRepository.saveAndFlush(
            new WebPushSubscription(
                subscription.endpoint, subscription.keys.auth, subscription.keys.p256dh)));
    _singleResultEntity.setSubscribers(subscribers);
    singleResultRepository.saveAndFlush(_singleResultEntity);
  }

  public void subscribeOneMany(Subscription subscription, String taskId) {
    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_oneManyResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
    List<WebPushSubscription> subscribers = _oneManyResultEntity.getSubscibers();
    subscribers.add(
        webPushRepository.saveAndFlush(
            new WebPushSubscription(
                subscription.endpoint, subscription.keys.auth, subscription.keys.p256dh)));
    _oneManyResultEntity.setSubscribers(subscribers);
    oneManyRepository.saveAndFlush(_oneManyResultEntity);
  }

  public void subscribeManyMany(Subscription subscription, String taskId) {
    ManyManyResultEntity _manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_manyManyResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
    List<WebPushSubscription> subscribers = _manyManyResultEntity.getSubscibers();
    subscribers.add(
        webPushRepository.saveAndFlush(
            new WebPushSubscription(
                subscription.endpoint, subscription.keys.auth, subscription.keys.p256dh)));
    _manyManyResultEntity.setSubscribers(subscribers);
    manyManyRepository.saveAndFlush(_manyManyResultEntity);
  }

  public void unSubscribeSingle(String endpoint, String taskId) {
    SingleResultEntity _singleResultEntity =
        singleResultRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_singleResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
    List<WebPushSubscription> subscribers =
        _singleResultEntity.getSubscibers().stream()
            .filter(s -> !endpoint.equals(s.getEndpoint()))
            .collect(Collectors.toList());

    _singleResultEntity.setSubscribers(subscribers);
    singleResultRepository.saveAndFlush(_singleResultEntity);
  }

  public void unSubscribeOneMany(String endpoint, String taskId) {
    OneManyResultEntity _oneManyResultEntity =
        oneManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_oneManyResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
    List<WebPushSubscription> subscribers =
        _oneManyResultEntity.getSubscibers().stream()
            .filter(s -> !endpoint.equals(s.getEndpoint()))
            .collect(Collectors.toList());

    _oneManyResultEntity.setSubscribers(subscribers);
    oneManyRepository.saveAndFlush(_oneManyResultEntity);
  }

  public void unSubscribeManyMany(String endpoint, String taskId) {
    ManyManyResultEntity _manyManyResultEntity =
        manyManyRepository.getByHashId(UUID.fromString(taskId));
    if (Objects.equals(_manyManyResultEntity, null)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "task does not exist");
    }
    List<WebPushSubscription> subscribers =
        _manyManyResultEntity.getSubscibers().stream()
            .filter(s -> !endpoint.equals(s.getEndpoint()))
            .collect(Collectors.toList());
    _manyManyResultEntity.setSubscribers(subscribers);
    manyManyRepository.saveAndFlush(_manyManyResultEntity);
  }

  public void sendNotification(Subscription subscription, String messageJson) {
    try {
      pushService.send(new Notification(subscription, messageJson));
    } catch (GeneralSecurityException
        | IOException
        | JoseException
        | ExecutionException
        | InterruptedException e) {
      e.printStackTrace();
    }
  }

  public void sendNotificationToClient(UUID clientId, String message) {
    WebPushSubscription subscription = webPushRepository.getByHashId(clientId);
    if (subscription != null) {
      try {
        PushService pushService = new PushService();
        pushService.setPublicKey(publicKey);
        pushService.setPrivateKey(privateKey);

        Notification notification =
            new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                message);

        pushService.send(notification);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void sendNotificationToClient(WebPushSubscription subscription, String message) {
    if (subscription != null) {
      try {
        PushService pushService = new PushService();
        pushService.setPublicKey(publicKey);
        pushService.setPrivateKey(privateKey);

        Notification notification =
            new Notification(
                subscription.getEndpoint(),
                subscription.getP256dh(),
                subscription.getAuth(),
                message);

        pushService.send(notification);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
