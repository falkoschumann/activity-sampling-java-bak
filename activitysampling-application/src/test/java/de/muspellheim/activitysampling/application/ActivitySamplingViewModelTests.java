package de.muspellheim.activitysampling.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.muspellheim.activitysampling.domain.*;
import de.muspellheim.activitysampling.domain.tests.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;

class ActivitySamplingViewModelTests {
  private StubActivitiesService model;
  private ActivitySamplingViewModel viewModel;

  @BeforeEach
  void init() {
    var clock =
        new TickingClock(
            Instant.parse("2022-10-22T06:13:00Z"), Duration.ofMinutes(1), ZoneId.systemDefault());
    model = new StubActivitiesService(clock);
    viewModel = new ActivitySamplingViewModel(model);
    viewModel.onCountdownElapsed = mock(Runnable.class);
  }

  @Test
  void run_InitializeView() {
    viewModel.run();

    assertAll(
        () -> assertEquals("", viewModel.clientProperty().get(), "Client"),
        () -> assertEquals("", viewModel.projectProperty().get(), "Project"),
        () -> assertEquals("", viewModel.taskProperty().get(), "Task"),
        () -> assertEquals("", viewModel.notesProperty().get(), "Notes"),
        () -> assertTrue(viewModel.logButtonDisabledProperty().get(), "Log button disabled"),
        () -> assertEquals(List.of(), viewModel.getRecentActivities(), "Recent activities"));
  }

  @Test
  void run_RestoreView() {
    model.setAllActivities(
        List.of(
            new Activity(
                Instant.parse("2022-10-20T19:09:00Z"),
                "Muspellheim",
                "Activity Sampling",
                "Analyze",
                "Taste JavaScript"),
            new Activity(
                Instant.parse("2022-10-20T19:29:00Z"),
                "Muspellheim",
                "Activity Sampling",
                "Maintenance",
                "Buy unicorn")));

    viewModel.run();

    assertAll(
        () -> assertEquals("", viewModel.clientProperty().get(), "Client"),
        () -> assertEquals("", viewModel.projectProperty().get(), "Project"),
        () -> assertEquals("", viewModel.taskProperty().get(), "Task"),
        () -> assertEquals("", viewModel.notesProperty().get(), "Notes"),
        () -> assertTrue(viewModel.logButtonDisabledProperty().get(), "Log button disabled"),
        () ->
            assertEquals(
                List.of(
                    "Donnerstag, 20. Oktober 2022",
                    "21:09 - Activity Sampling (Muspellheim) Analyze - Taste JavaScript",
                    "21:29 - Activity Sampling (Muspellheim) Maintenance - Buy unicorn"),
                viewModel.getRecentActivities(),
                "Recent activities"));
  }

  @Test
  void logButtonEnabledIfFormIsNotEmpty() {
    viewModel.run();

    viewModel.projectProperty().set("project");
    viewModel.taskProperty().set("task");
    viewModel.notesProperty().set("notes");
    viewModel.clientProperty().set("client");

    assertFalse(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfFormIsEmpty() {
    viewModel.run();

    viewModel.projectProperty().set("");
    viewModel.taskProperty().set("");
    viewModel.notesProperty().set("");
    viewModel.clientProperty().set("");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfFormIsBlank() {
    viewModel.run();

    viewModel.projectProperty().set("  ");
    viewModel.taskProperty().set("  ");
    viewModel.notesProperty().set("  ");
    viewModel.clientProperty().set("  ");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfClientIsEmpty() {
    viewModel.run();

    viewModel.clientProperty().set("");
    viewModel.projectProperty().set("project");
    viewModel.taskProperty().set("task");
    viewModel.notesProperty().set("notes");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfClientIsBlank() {
    viewModel.run();

    viewModel.clientProperty().set("  ");
    viewModel.projectProperty().set("project");
    viewModel.taskProperty().set("task");
    viewModel.notesProperty().set("notes");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfProjectIsEmpty() {
    viewModel.run();

    viewModel.clientProperty().set("client");
    viewModel.projectProperty().set("");
    viewModel.taskProperty().set("task");
    viewModel.notesProperty().set("notes");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfProjectIsBlank() {
    viewModel.run();

    viewModel.clientProperty().set("client");
    viewModel.projectProperty().set("  ");
    viewModel.taskProperty().set("task");
    viewModel.notesProperty().set("notes");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfTaskIsEmpty() {
    viewModel.run();

    viewModel.clientProperty().set("client");
    viewModel.projectProperty().set("project");
    viewModel.taskProperty().set("");
    viewModel.notesProperty().set("notes");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfTaskIsBlank() {
    viewModel.run();

    viewModel.clientProperty().set("client");
    viewModel.projectProperty().set("project");
    viewModel.taskProperty().set("  ");
    viewModel.notesProperty().set("notes");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfNotesIsEmpty() {
    viewModel.run();

    viewModel.clientProperty().set("client");
    viewModel.projectProperty().set("project");
    viewModel.taskProperty().set("task");
    viewModel.notesProperty().set("");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logButtonDisabledIfNotesIsBlank() {
    viewModel.run();

    viewModel.clientProperty().set("client");
    viewModel.projectProperty().set("project");
    viewModel.taskProperty().set("task");
    viewModel.notesProperty().set("  ");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logActivity_AddNewActivityAndResetForm() {
    viewModel.run();
    viewModel.clientProperty().set("Muspellheim");
    viewModel.projectProperty().set("Activity Sampling");
    viewModel.taskProperty().set("Testing");
    viewModel.notesProperty().set("Taste JavaScript");

    viewModel.logActivity();

    assertAll(
        () -> assertEquals("Muspellheim", viewModel.clientProperty().get(), "Client"),
        () -> assertEquals("Activity Sampling", viewModel.projectProperty().get(), "Project"),
        () -> assertEquals("Testing", viewModel.taskProperty().get(), "Task"),
        () -> assertEquals("Taste JavaScript", viewModel.notesProperty().get(), "Notes"),
        () -> assertFalse(viewModel.logButtonDisabledProperty().get(), "Log button disabled"),
        () ->
            assertEquals(
                List.of(
                    "Samstag, 22. Oktober 2022",
                    "08:13 - Activity Sampling (Muspellheim) Testing - Taste JavaScript"),
                viewModel.getRecentActivities(),
                "Recent activities"));
  }

  @Test
  void startCountdown_InitializesCountdown() {
    viewModel.run();

    viewModel.startCountdown(Duration.ofMinutes(20));

    assertAll(
        () -> assertEquals("00:20:00", viewModel.countdownTextProperty().get(), "Countdown text"),
        () -> assertEquals(0.0, viewModel.countdownProgressProperty().get(), "Countdown progress"),
        () -> verify(viewModel.onCountdownElapsed, times(0)).run());
  }

  @Test
  void progressCountdown_FirstTick() {
    viewModel.run();
    viewModel.startCountdown(Duration.ofMinutes(1));

    tickCountdown(1);

    assertAll(
        () -> assertEquals("00:00:59", viewModel.countdownTextProperty().get(), "Countdown text"),
        () ->
            assertEquals(
                1.0 / 60.0, viewModel.countdownProgressProperty().get(), "Countdown progress"),
        () -> verify(viewModel.onCountdownElapsed, times(0)).run());
  }

  @Test
  void progressCountdown_SecondTick() {
    viewModel.run();
    viewModel.startCountdown(Duration.ofMinutes(1));

    tickCountdown(2);

    assertAll(
        () -> assertEquals("00:00:58", viewModel.countdownTextProperty().get(), "Countdown text"),
        () ->
            assertEquals(
                2.0 / 60.0, viewModel.countdownProgressProperty().get(), "Countdown progress"),
        () -> verify(viewModel.onCountdownElapsed, times(0)).run());
  }

  @Test
  void progressCountdown_LastTick() {
    viewModel.run();
    viewModel.startCountdown(Duration.ofMinutes(1));

    tickCountdown(60);

    assertAll(
        () -> assertEquals("00:00:00", viewModel.countdownTextProperty().get(), "Countdown text"),
        () -> assertEquals(1.0, viewModel.countdownProgressProperty().get(), "Countdown progress"),
        () -> verify(viewModel.onCountdownElapsed, times(0)).run());
  }

  @Test
  void progressCountdown_CountdownElapsed() {
    viewModel.run();
    viewModel.startCountdown(Duration.ofMinutes(1));

    tickCountdown(61);

    assertAll(
        () -> assertEquals("00:01:00", viewModel.countdownTextProperty().get(), "Countdown text"),
        () -> assertEquals(0.0, viewModel.countdownProgressProperty().get(), "Countdown progress"),
        () -> verify(viewModel.onCountdownElapsed, times(1)).run());
  }

  private void tickCountdown(int count) {
    for (var i = 0; i < count; i++) {
      viewModel.progressCountdown();
    }
  }
}
