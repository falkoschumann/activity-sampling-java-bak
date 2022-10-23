package de.muspellheim.activitysampling.domain;

import java.time.*;
import java.util.*;

public record ActivityLoggedEvent(Instant timestamp, String activity) implements Event {
  public ActivityLoggedEvent {
    Objects.requireNonNull(timestamp, "timestamp");
    Objects.requireNonNull(activity, "activity");
  }
}
