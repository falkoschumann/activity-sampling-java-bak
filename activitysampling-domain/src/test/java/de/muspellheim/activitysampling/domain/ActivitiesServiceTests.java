package de.muspellheim.activitysampling.domain;

import static org.hamcrest.MatcherAssert.*;
import static org.junit.jupiter.api.Assertions.*;

import de.muspellheim.activitysampling.domain.tests.*;
import java.time.*;
import java.util.*;
import org.hamcrest.collection.*;
import org.junit.jupiter.api.*;

class ActivitiesServiceTests {
  private ActivitiesService service;
  private TickingClock clock;
  private EventStore eventStore;

  @BeforeEach
  void init() {
    eventStore = new FakeEventStore();
    clock =
        new TickingClock(
            Instant.parse("2022-10-19T21:44:00Z"), Duration.ofMinutes(1), ZoneId.systemDefault());
    service = new ActivitiesServiceImpl(eventStore, clock);
  }

  @Test
  void logFirstActivity() {
    service.logActivity("client", Duration.ofMinutes(1), "project", "task", "description");

    assertThat(
        service.selectAllActivities(),
        IsIterableContainingInOrder.contains(
            new Activity(
                Instant.parse("2022-10-19T21:44:00Z"),
                Duration.ofMinutes(1),
                "client",
                "project",
                "task",
                "description")));
  }

  @Test
  void logSecondActivity() {
    service.logActivity("client 1", Duration.ofMinutes(5), "project 1", "task 1", "description 1");
    clock.tick();

    service.logActivity("client 2", Duration.ofMinutes(10), "project 2", "task 2", "description 2");

    List<Activity> actual = service.selectAllActivities();
    assertThat(
        Collections.unmodifiableList(actual),
        IsIterableContainingInOrder.contains(
            new Activity(
                Instant.parse("2022-10-19T21:44:00Z"),
                Duration.ofMinutes(5),
                "client 1",
                "project 1",
                "task 1",
                "description 1"),
            new Activity(
                Instant.parse("2022-10-19T21:45:00Z"),
                Duration.ofMinutes(10),
                "client 2",
                "project 2",
                "task 2",
                "description 2")));
  }

  @Test
  void selectRecentActivities() {
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-19T18:20:00Z"),
            Duration.ofMinutes(20),
            "client 1",
            "project 1",
            "task 1",
            "notes 1"));
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-18T18:20:00Z"),
            Duration.ofMinutes(20),
            "client 2",
            "project 2",
            "task 2",
            "notes 2"));
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-17T18:20:00Z"),
            Duration.ofMinutes(20),
            "client 3",
            "project 3",
            "task 3",
            "notes 3"));
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-01T18:20:00Z"),
            Duration.ofMinutes(20),
            "client 4",
            "project 4",
            "task 4",
            "notes 4"));

    var recentActivities = service.selectRecentActivities(2);

    assertEquals(
        List.of(
            new Activity(
                Instant.parse("2022-10-19T18:20:00Z"),
                Duration.ofMinutes(20),
                "client 1",
                "project 1",
                "task 1",
                "notes 1"),
            new Activity(
                Instant.parse("2022-10-18T18:20:00Z"),
                Duration.ofMinutes(20),
                "client 2",
                "project 2",
                "task 2",
                "notes 2")),
        recentActivities);
  }

  @Test
  void calculateTimeSummary() {
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-19T18:20:00Z"), Duration.ofMinutes(20), "", "", "", ""));
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-18T18:20:00Z"), Duration.ofMinutes(20), "", "", "", ""));
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-17T18:20:00Z"), Duration.ofMinutes(20), "", "", "", ""));
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-10-01T18:20:00Z"), Duration.ofMinutes(20), "", "", "", ""));
    eventStore.record(
        new ActivityLoggedEvent(
            Instant.parse("2022-09-30T23:59:59+02:00"), Duration.ofMinutes(20), "", "", "", ""));

    var timeSummary = service.calculateTimeSummary();

    assertEquals(
        new TimeSummary(
            Duration.ofMinutes(20),
            Duration.ofMinutes(20),
            Duration.ofMinutes(60),
            Duration.ofMinutes(80)),
        timeSummary);
  }
}
