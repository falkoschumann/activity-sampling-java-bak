package de.muspellheim.activitysampling.domain;

import java.time.*;
import java.util.*;

public record ActivityLoggedEvent(
    Instant timestamp, String client, String project, String task, String notes) implements Event {
  public ActivityLoggedEvent {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(client, "client");
    Objects.requireNonNull(project, "project");
    Objects.requireNonNull(task, "task");
    Objects.requireNonNull(notes, "notes");
  }
}
