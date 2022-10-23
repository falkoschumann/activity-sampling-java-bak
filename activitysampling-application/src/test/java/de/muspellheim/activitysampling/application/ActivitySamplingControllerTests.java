package de.muspellheim.activitysampling.application;

import static org.junit.jupiter.api.Assertions.*;

import de.muspellheim.activitysampling.domain.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;

class ActivitySamplingControllerTests {
  private TickingClock clock;
  private StubActivitiesService model;
  private FakeActivitySamplingView view;
  private ActivitySamplingController controller;

  @BeforeAll
  static void initAll() {
    Locale.setDefault(Locale.GERMANY);
  }

  @BeforeEach
  void init() {
    clock =
        new TickingClock(
            Instant.parse("2022-10-22T06:13:00Z"), Duration.ofMinutes(1), ZoneId.systemDefault());
    model = new StubActivitiesService(clock);
    view = new FakeActivitySamplingView();
    controller = new ActivitySamplingController(model, view);
  }

  @Test
  void run_InitializeView() {
    controller.run();

    assertAll(
        () -> assertEquals("", view.getActivityText(), "Activity text"),
        () -> assertFalse(view.isLogButtonEnabled(), "Log button enabled"),
        () -> assertEquals(List.of(), view.getRecentActivities(), "Recent activities"),
        () -> assertEquals(1, view.getShowCallCounter(), "Show call counter"));
  }

  @Test
  void run_RestoreView() {
    model.setAllActivities(
        List.of(
            new Activity(Instant.parse("2022-10-20T19:09:00Z"), "Taste JavaScript"),
            new Activity(Instant.parse("2022-10-20T19:29:00Z"), "Buy unicorn")));

    controller.run();

    assertAll(
        () -> assertEquals("", view.getActivityText(), "Activity test"),
        () -> assertFalse(view.isLogButtonEnabled(), "Log button enabled"),
        () ->
            assertEquals(
                List.of(
                    "Donnerstag, 20. Oktober 2022",
                    "21:09 - Taste JavaScript",
                    "21:29 - Buy unicorn"),
                view.getRecentActivities(),
                "Recent activities"),
        () -> assertEquals(1, view.getShowCallCounter(), "Show call counter"));
  }

  @Test
  void activityTextChanged_LogButtonEnabledIfActivityTextIsNotEmpty() {
    controller.run();
    view.setActivityText("f");

    controller.activityTextChanged();

    assertTrue(view.isLogButtonEnabled());
  }

  @Test
  void activityTextChanged_LogButtonDisabledIfActivityTextIsBlank() {
    controller.run();
    view.setActivityText(" ");

    controller.activityTextChanged();

    assertFalse(view.isLogButtonEnabled());
  }

  @Test
  void logActivity_AddNewActivityAndResetForm() {
    controller.run();
    view.setActivityText("Taste JavaScript");

    controller.logActivity();

    assertAll(
        () -> assertEquals("", view.getActivityText(), "Activity text"),
        () -> assertFalse(view.isLogButtonEnabled(), "Log button enabled"),
        () ->
            assertEquals(
                List.of("Samstag, 22. Oktober 2022", "08:13 - Taste JavaScript"),
                view.getRecentActivities(),
                "Recent activities"));
  }

  @Test
  void startCountdown_InitializesCountdown() {
    controller.run();

    controller.startCountdown(Duration.ofMinutes(20));

    assertAll(
        () -> assertEquals("00:20:00", view.getCountdownLabelText(), "Countdown label text"),
        () -> assertEquals(1200, view.getCountdownProgressMaximum(), "Countdown max value"),
        () -> assertEquals(0, view.getCountdownProgressValue(), "Countdown current value"),
        () -> assertEquals(1, view.getStartCountdownCallCounter(), "Start countdown call counter"),
        () ->
            assertEquals(
                0, view.getCountdownElapsedCallCounter(), "Countdown elapsed call counter"));
  }

  @Test
  void stopCountdown_HaltsCountdown() {
    controller.run();
    controller.startCountdown(Duration.ofMinutes(20));

    controller.stopCountdown();

    assertAll(
        () -> assertEquals(1, view.getStopCountdownCallCounter(), "Stop countdown call counter"),
        () ->
            assertEquals(
                0, view.getCountdownElapsedCallCounter(), "Countdown elapsed call counter"));
  }

  @Test
  void ProgressCountdown_FirstTick() {
    controller.run();
    controller.startCountdown(Duration.ofMinutes(1));

    tickCountdown(1);

    assertAll(
        () -> assertEquals("00:00:59", view.getCountdownLabelText(), "Countdown label text"),
        () -> assertEquals(60, view.getCountdownProgressMaximum(), "Countdown max value"),
        () -> assertEquals(1, view.getCountdownProgressValue(), "Countdown current value"),
        () ->
            assertEquals(
                0, view.getCountdownElapsedCallCounter(), "Countdown elapsed call counter"));
  }

  @Test
  void ProgressCountdown_SecondTick() {
    controller.run();
    controller.startCountdown(Duration.ofMinutes(1));

    tickCountdown(2);

    assertAll(
        () -> assertEquals("00:00:58", view.getCountdownLabelText(), "Countdown label text"),
        () -> assertEquals(60, view.getCountdownProgressMaximum(), "Countdown max value"),
        () -> assertEquals(2, view.getCountdownProgressValue(), "Countdown current value"),
        () ->
            assertEquals(
                0, view.getCountdownElapsedCallCounter(), "Countdown elapsed call counter"));
  }

  @Test
  void ProgressCountdown_LastTick() {
    controller.run();
    controller.startCountdown(Duration.ofMinutes(1));

    tickCountdown(60);

    assertAll(
        () -> assertEquals("00:00:00", view.getCountdownLabelText(), "Countdown label text"),
        () -> assertEquals(60, view.getCountdownProgressMaximum(), "Countdown max value"),
        () -> assertEquals(60, view.getCountdownProgressValue(), "Countdown current value"),
        () ->
            assertEquals(
                0, view.getCountdownElapsedCallCounter(), "Countdown elapsed call counter"));
  }

  @Test
  void ProgressCountdown_CountdownElapsed() {
    controller.run();
    controller.startCountdown(Duration.ofMinutes(1));

    tickCountdown(61);

    assertAll(
        () -> assertEquals("00:01:00", view.getCountdownLabelText(), "Countdown label text"),
        () -> assertEquals(60, view.getCountdownProgressMaximum(), "Countdown max value"),
        () -> assertEquals(0, view.getCountdownProgressValue(), "Countdown current value"),
        () ->
            assertEquals(
                1, view.getCountdownElapsedCallCounter(), "Countdown elapsed call counter"));
  }

  private void tickCountdown(int count) {
    for (var i = 0; i < count; i++) {
      controller.progressCountdown();
    }
  }
}
