package de.muspellheim.activitysampling.application;

import static org.junit.jupiter.api.Assertions.*;

import de.muspellheim.activitysampling.domain.*;
import de.muspellheim.activitysampling.domain.tests.*;
import de.muspellheim.activitysampling.infrastructure.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;

class AcceptanceTest {
  private final Path file = Paths.get("build/activity-log.csv");
  private TickingClock clock;
  private ActivitySamplingViewModel viewModel;

  @BeforeEach
  void init() throws IOException {
    Files.deleteIfExists(file);
    var eventStore = new CsvEventStore(file);
    clock =
        new TickingClock(
            Instant.parse("2022-10-26T18:13:00Z"),
            Duration.ofSeconds(1),
            ZoneId.of("Europe/Berlin"));
    var model = new ActivitiesServiceImpl(eventStore, clock);
    viewModel = new ActivitySamplingViewModel(model);
  }

  @Test
  void scenario() {
    // Initialize
    viewModel.run();
    assertTrue(viewModel.logButtonDisabledProperty().get());

    // Start countdown
    viewModel.startCountdown(Duration.ofMinutes(15));

    // Progress countdown
    tickCountdown(Duration.ofMinutes(6));
    assertEquals("00:09:00", viewModel.countdownTextProperty().get());
    assertEquals(0.4, viewModel.countdownProgressProperty().get());

    // Countdown elapsed
    tickCountdown(Duration.ofMinutes(9));
    assertEquals("00:15:00", viewModel.countdownTextProperty().get());
    assertEquals(0.0, viewModel.countdownProgressProperty().get());

    // Log first activity
    viewModel.clientProperty().set("Muspellheim");
    viewModel.projectProperty().set("Activity Sampling");
    viewModel.taskProperty().set("Analyze");
    viewModel.notesProperty().set("Taste JavaScript");
    assertFalse(viewModel.logButtonDisabledProperty().get());
    viewModel.logActivity();

    // Progress countdown
    tickCountdown(Duration.ofMinutes(9));
    assertEquals("00:06:00", viewModel.countdownTextProperty().get());
    assertEquals(0.6, viewModel.countdownProgressProperty().get());

    // Countdown elapsed
    tickCountdown(Duration.ofMinutes(6));
    assertEquals("00:15:00", viewModel.countdownTextProperty().get());
    assertEquals(0.0, viewModel.countdownProgressProperty().get());

    // Log second activity
    viewModel.clientProperty().set("Muspellheim");
    viewModel.projectProperty().set("Activity Sampling");
    viewModel.taskProperty().set("Maintenance");
    viewModel.notesProperty().set("Buy unicorn");
    assertFalse(viewModel.logButtonDisabledProperty().get());
    viewModel.logActivity();

    assertEquals(
        List.of(
            new ActivityItem("Mittwoch, 26. Oktober 2022"),
            new ActivityItem(
                "20:28 - Activity Sampling (Muspellheim) Analyze - Taste JavaScript",
                new Activity(
                    Instant.parse("2022-10-26T18:28:00Z"),
                    Duration.ofMinutes(15),
                    "Muspellheim",
                    "Activity Sampling",
                    "Analyze",
                    "Taste JavaScript")),
            new ActivityItem(
                "20:43 - Activity Sampling (Muspellheim) Maintenance - Buy unicorn",
                new Activity(
                    Instant.parse("2022-10-26T18:43:00Z"),
                    Duration.ofMinutes(15),
                    "Muspellheim",
                    "Activity Sampling",
                    "Maintenance",
                    "Buy unicorn"))),
        viewModel.getRecentActivities());
    assertEquals("00:30", viewModel.hoursTodayProperty().get());
    assertEquals("00:00", viewModel.hoursYesterdayProperty().get());
    assertEquals("00:30", viewModel.hoursThisWeekProperty().get());
    assertEquals("00:30", viewModel.hoursThisMonthProperty().get());
  }

  private void tickCountdown(Duration duration) {
    for (var i = 0; i < duration.toSeconds(); i++) {
      viewModel.progressCountdown();
      clock.tick();
    }
  }
}
