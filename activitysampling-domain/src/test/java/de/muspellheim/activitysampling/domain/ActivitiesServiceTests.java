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
    service.logActivity("foo");

    assertThat(
        service.selectAllActivities(),
        IsIterableContainingInOrder.contains(
            new Activity(Instant.parse("2022-10-18T21:44:00Z"), "foo")));
  }

  @Test
  void logSecondActivity() {
    service.logActivity("foo");
    clock.tick();

    service.logActivity("bar");

    List<Activity> actual = service.selectAllActivities();
    assertThat(
        Collections.unmodifiableList(actual),
        IsIterableContainingInOrder.contains(
            new Activity(Instant.parse("2022-10-18T21:44:00Z"), "foo"),
            new Activity(Instant.parse("2022-10-18T21:45:00Z"), "bar")));
  }
}
