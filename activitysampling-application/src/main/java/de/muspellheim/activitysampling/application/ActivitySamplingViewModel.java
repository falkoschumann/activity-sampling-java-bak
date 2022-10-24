package de.muspellheim.activitysampling.application;

import de.muspellheim.activitysampling.domain.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javafx.beans.property.*;
import javafx.collections.*;

class ActivitySamplingViewModel {
  public Runnable onCountdownElapsed;

  private final StringProperty activity =
      new SimpleStringProperty("") {
        @Override
        protected void invalidated() {
          logButtonDisabled.set(activity.get().isBlank());
        }
      };
  private final ReadOnlyBooleanWrapper logButtonDisabled = new ReadOnlyBooleanWrapper(true);
  private final ReadOnlyStringWrapper countdownText = new ReadOnlyStringWrapper();
  private final ReadOnlyDoubleWrapper countdownProgress = new ReadOnlyDoubleWrapper();
  private final ObservableList<String> recentActivities = FXCollections.observableArrayList();
  private final ActivitiesService model;

  private Duration interval;
  private Duration countdown;

  public ActivitySamplingViewModel(ActivitiesService model) {
    this.model = model;
  }

  StringProperty activityProperty() {
    return activity;
  }

  ReadOnlyBooleanProperty logButtonDisabledProperty() {
    return logButtonDisabled.getReadOnlyProperty();
  }

  ObservableList<String> getRecentActivities() {
    return recentActivities;
  }

  ReadOnlyStringProperty countdownTextProperty() {
    return countdownText;
  }

  ReadOnlyDoubleProperty countdownProgressProperty() {
    return countdownProgress;
  }

  void run() {
    updateRecentActivities();
  }

  void logActivity() {
    model.logActivity(activityProperty().get());
    updateRecentActivities();
  }

  private void updateRecentActivities() {
    var activities = model.selectAllActivities();
    var formattedActivities = formattedRecentActivities(activities);
    recentActivities.setAll(formattedActivities);
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

  void startCountdown(Duration interval) {
    this.interval = interval;
    this.countdown = interval;

    updateProgress();
  }

  void progressCountdown() {
    if (countdown.isZero()) {
      Optional.ofNullable(onCountdownElapsed).ifPresent(Runnable::run);
      countdown = interval;
    } else {
      countdown = countdown.minusSeconds(1);
    }
    updateProgress();
  }

  private void updateProgress() {
    var time = LocalTime.ofSecondOfDay(countdown.toSeconds());
    var formattedTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).format(time);
    countdownText.set(formattedTime);
    var elapsedSeconds = (double) interval.minus(countdown).getSeconds();
    countdownProgress.set(elapsedSeconds / interval.toSeconds());
  }
}
