package de.muspellheim.activitysampling.domain;

import java.time.*;
import java.util.*;
import java.util.stream.*;

public class ActivitiesServiceImpl implements ActivitiesService {
  private final EventStore eventStore;
  private final Clock clock;

  public ActivitiesServiceImpl(EventStore eventStore) {
    this(eventStore, Clock.systemDefaultZone());
  }

  ActivitiesServiceImpl(EventStore eventStore, Clock clock) {
    this.eventStore = eventStore;
    this.clock = clock;
  }

  @Override
  public void logActivity(String client, String project, String task, String notes) {
    ActivityLoggedEvent event =
        new ActivityLoggedEvent(Instant.now(clock), client, project, task, notes);
    eventStore.record(event);
  }

  @Override
  public List<Activity> selectAllActivities() {
    return StreamSupport.stream(eventStore.replay().spliterator(), false)
        .filter((event -> event instanceof ActivityLoggedEvent))
        .map(event -> (ActivityLoggedEvent) event)
        .map(
            event ->
                new Activity(
                    event.timestamp(),
                    event.client(),
                    event.project(),
                    event.task(),
                    event.notes()))
        .toList();
  }
}
