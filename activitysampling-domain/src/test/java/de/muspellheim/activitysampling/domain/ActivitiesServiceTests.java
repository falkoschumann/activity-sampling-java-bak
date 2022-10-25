package de.muspellheim.activitysampling.domain;

import static org.hamcrest.MatcherAssert.*;

import de.muspellheim.activitysampling.domain.tests.*;
import java.time.*;
import java.util.*;
import org.hamcrest.collection.*;
import org.junit.jupiter.api.*;

class ActivitiesServiceTests {
  private ActivitiesService service;
  private TickingClock clock;

  @BeforeEach
  void init() {
    var eventStore = new FakeEventStore();
    clock =
        new TickingClock(
            Instant.parse("2022-10-18T21:44:00Z"), Duration.ofMinutes(1), ZoneId.systemDefault());
    service = new ActivitiesServiceImpl(eventStore, clock);
  }

  @Test
  void logFirstActivity() {
    service.logActivity("client", "project", "task", "description");

    assertThat(
        service.selectAllActivities(),
        IsIterableContainingInOrder.contains(
            new Activity(
                Instant.parse("2022-10-18T21:44:00Z"),
                "client",
                "project",
                "task",
                "description")));
  }

  @Test
  void logSecondActivity() {
    service.logActivity("client 1", "project 1", "task 1", "description 1");
    clock.tick();

    service.logActivity("client 2", "project 2", "task 2", "description 2");

    List<Activity> actual = service.selectAllActivities();
    assertThat(
        Collections.unmodifiableList(actual),
        IsIterableContainingInOrder.contains(
            new Activity(
                Instant.parse("2022-10-18T21:44:00Z"),
                "client 1",
                "project 1",
                "task 1",
                "description 1"),
            new Activity(
                Instant.parse("2022-10-18T21:45:00Z"),
                "client 2",
                "project 2",
                "task 2",
                "description 2")));
  }
}
