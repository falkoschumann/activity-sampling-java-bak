package de.muspellheim.activitysampling.domain;

import java.util.*;

class FakeEventStore implements EventStore {
  private final List<Event> events = new ArrayList<>();

  @Override
  public void record(Event event) {
    events.add(event);
  }

  @Override
  public Iterable<Event> replay() {
    return events;
  }
}
