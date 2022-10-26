package de.muspellheim.activitysampling.domain;

import java.time.*;
import java.util.*;

public record Activity(
    Instant timestamp,
    Duration duration,
    String client,
    String project,
    String task,
    String notes) {
  public Activity {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(duration, "duration");
    Objects.requireNonNull(client, "client");
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(task, "task");
    Objects.requireNonNull(notes, "notes");
  }
}
