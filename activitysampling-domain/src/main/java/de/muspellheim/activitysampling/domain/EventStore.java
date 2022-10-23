package de.muspellheim.activitysampling.domain;

public interface EventStore {
  void record(Event event);

  Iterable<? extends Event> replay();
}
