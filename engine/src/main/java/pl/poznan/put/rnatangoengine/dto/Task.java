package pl.poznan.put.rnatangoengine.dto;

public class Task {
  String taskHashId;
  TaskType type;

  public Task(String taskHashId, TaskType type) {
    this.taskHashId = taskHashId;
    this.type = type;
  }

  public String toString() {
    return this.taskHashId + ";" + type.toString();
  }
}
