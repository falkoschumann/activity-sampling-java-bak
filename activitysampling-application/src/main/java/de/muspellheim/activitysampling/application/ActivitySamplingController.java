package de.muspellheim.activitysampling.application;

import de.muspellheim.activitysampling.domain.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

class ActivitySamplingController implements ActivitySamplingViewDelegate {
  private final ActivitiesService model;
  private final ActivitySamplingView view;
  private Duration interval;
  private Duration countdown;

  ActivitySamplingController(ActivitiesService model, ActivitySamplingView view) {
    this.model = model;
    this.view = view;
    view.initDelegate(this);
  }

  public void run() {
    loadView();
    view.show();
  }

  @Override
  public void activityTextChanged() {
    updateLogButtonEnabled();
  }

  @Override
  public void logActivity() {
    model.logActivity(view.getActivityText());
    loadView();
  }

  @Override
  public void startCountdown(Duration interval) {
    this.interval = interval;
    this.countdown = interval;
    loadProgress();
    this.view.startCountdown();
  }

  private void loadView() {
    view.setActivityText("");
    updateLogButtonEnabled();

    var activities = model.selectAllActivities();
    var formattedActivities = formattedRecentActivities(activities);
    view.setRecentActivities(formattedActivities);
  }

  private List<String> formattedRecentActivities(List<Activity> activities) {
    var rows = new ArrayList<String>();
    for (var activity : activities) {
      var dateTime = LocalDateTime.ofInstant(activity.timestamp(), ZoneId.systemDefault());
      if (rows.isEmpty()) {
        var formattedDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(dateTime);
        rows.add(formattedDate);
      }
      var formattedTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(dateTime);
      rows.add(formattedTime + " - " + activity.description());
    }
    return rows;
  }

  private void updateLogButtonEnabled() {
    var enabled = !view.getActivityText().isBlank();
    view.setLogButtonEnabled(enabled);
  }

  private void loadProgress() {
    view.setCountdownProgressMaximum((int) interval.toSeconds());
    var time = LocalTime.ofSecondOfDay(countdown.toSeconds());
    var formattedTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(time);
    view.setCountdownLabelText(formattedTime);
    int elapsedSeconds = (int) interval.minus(countdown).getSeconds();
    view.setCountdownProgressValue(elapsedSeconds);
  }

  @Override
  public void stopCountdown() {
    view.stopCountdown();
  }

  @Override
  public void progressCountdown() {
    if (countdown.isZero()) {
      view.countdownElapsed();
      countdown = interval;
    } else {
      countdown = countdown.minusSeconds(1);
    }
    loadProgress();
  }
}
