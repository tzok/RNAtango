package pl.poznan.put.rnatangoengine.database.definitions;

import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import java.util.UUID;

@Entity
public class WebPushSubscription {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID hashId;

  private String endpoint;
  private String auth;
  private String p256dh;

  public WebPushSubscription() {}

  public WebPushSubscription(String endpoint, String auth, String p256dh) {
    this.endpoint = endpoint;
    this.auth = auth;
    this.p256dh = p256dh;
  }

  public String getEndpoint() {
    return this.endpoint;
  }

  public String getP256dh() {
    return this.p256dh;
  }

  public String getAuth() {
    return this.auth;
  }
}
