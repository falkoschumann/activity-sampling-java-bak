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
        () -> assertEquals("", viewModel.activityProperty().get(), "Activity"),
        () -> assertTrue(viewModel.logButtonDisabledProperty().get(), "Log button disabled"),
        () -> assertEquals(List.of(), viewModel.getRecentActivities(), "Recent activities"));
  }

  @Test
  void run_RestoreView() {
    model.setAllActivities(
        List.of(
            new Activity(Instant.parse("2022-10-20T19:09:00Z"), "Taste JavaScript"),
            new Activity(Instant.parse("2022-10-20T19:29:00Z"), "Buy unicorn")));

    viewModel.run();

    assertAll(
        () -> assertEquals("", viewModel.activityProperty().get(), "Activity"),
        () -> assertTrue(viewModel.logButtonDisabledProperty().get(), "Log button disabled"),
        () ->
            assertEquals(
                List.of(
                    "Donnerstag, 20. Oktober 2022",
                    "21:09 - Taste JavaScript",
                    "21:29 - Buy unicorn"),
                viewModel.getRecentActivities(),
                "Recent activities"));
  }

  @Test
  void activityTextChanged_LogButtonEnabledIfActivityTextIsNotEmpty() {
    viewModel.run();

    viewModel.activityProperty().set("f");

    assertFalse(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void activityTextChanged_LogButtonDisabledIfActivityTextIsBlank() {
    viewModel.run();

    viewModel.activityProperty().set(" ");

    assertTrue(viewModel.logButtonDisabledProperty().get());
  }

  @Test
  void logActivity_AddNewActivityAndResetForm() {
    viewModel.run();
    viewModel.activityProperty().set("Taste JavaScript");

    viewModel.logActivity();

    assertAll(
        () -> assertEquals("Taste JavaScript", viewModel.activityProperty().get(), "Activity"),
        () -> assertFalse(viewModel.logButtonDisabledProperty().get(), "Log button disabled"),
        () ->
            assertEquals(
                List.of("Samstag, 22. Oktober 2022", "08:13 - Taste JavaScript"),
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
