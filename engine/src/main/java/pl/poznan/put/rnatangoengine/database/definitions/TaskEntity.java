package pl.poznan.put.rnatangoengine.database.definitions;

import pl.poznan.put.rnatangoengine.dto.Status;

public class TaskEntity {

  protected String errorLog;
  protected Status status;

  public void setErrorLog(String errorLog) {
    this.errorLog = errorLog;
  }

  public String getErrorLog() {
    return this.errorLog;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Status getStatus() {
    return this.status;
  }
}
