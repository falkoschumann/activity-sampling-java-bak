package de.muspellheim.activitysampling.application;

import de.muspellheim.activitysampling.domain.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import javafx.beans.property.*;
import javafx.collections.*;

class ActivitySamplingViewModel {
  Runnable onCountdownElapsed;

  private final StringProperty client =
      new SimpleStringProperty("") {
        @Override
        protected void invalidated() {
          updateLogButtonDisabled();
        }
      };

  private final StringProperty project =
      new SimpleStringProperty("") {
        @Override
        protected void invalidated() {
          updateLogButtonDisabled();
        }
      };
  private final StringProperty task =
      new SimpleStringProperty("") {
        @Override
        protected void invalidated() {
          updateLogButtonDisabled();
        }
      };
  private final StringProperty notes =
      new SimpleStringProperty("") {
        @Override
        protected void invalidated() {
          updateLogButtonDisabled();
        }
      };
  private final ReadOnlyBooleanWrapper logButtonDisabled = new ReadOnlyBooleanWrapper(true);
  private final ReadOnlyStringWrapper countdownText = new ReadOnlyStringWrapper();
  private final ReadOnlyDoubleWrapper countdownProgress = new ReadOnlyDoubleWrapper();
  private final ObservableList<ActivityItem> recentActivities = FXCollections.observableArrayList();
  private final ReadOnlyStringWrapper hoursToday = new ReadOnlyStringWrapper("00:00");
  private final ReadOnlyStringWrapper hoursYesterday = new ReadOnlyStringWrapper("00:00");
  private final ReadOnlyStringWrapper hoursThisWeek = new ReadOnlyStringWrapper("00:00");
  private final ReadOnlyStringWrapper hoursThisMonth = new ReadOnlyStringWrapper("00:00");
  private final ActivitiesService model;

  private Duration interval = Duration.ofMinutes(20);
  private Duration countdown;

  ActivitySamplingViewModel(ActivitiesService model) {
    this.model = model;
  }

  StringProperty clientProperty() {
    return client;
  }

  StringProperty projectProperty() {
    return project;
  }

  StringProperty taskProperty() {
    return task;
  }

  StringProperty notesProperty() {
    return notes;
  }

  ReadOnlyBooleanProperty logButtonDisabledProperty() {
    return logButtonDisabled.getReadOnlyProperty();
  }

  ReadOnlyStringProperty countdownTextProperty() {
    return countdownText.getReadOnlyProperty();
  }

  ReadOnlyDoubleProperty countdownProgressProperty() {
    return countdownProgress.getReadOnlyProperty();
  }

  ObservableList<ActivityItem> getRecentActivities() {
    return recentActivities;
  }

  ReadOnlyStringProperty hoursTodayProperty() {
    return hoursToday.getReadOnlyProperty();
  }

  ReadOnlyStringProperty hoursYesterdayProperty() {
    return hoursYesterday.getReadOnlyProperty();
  }

  ReadOnlyStringProperty hoursThisWeekProperty() {
    return hoursThisWeek.getReadOnlyProperty();
  }

  ReadOnlyStringProperty hoursThisMonthProperty() {
    return hoursThisMonth.getReadOnlyProperty();
  }

  private void updateLogButtonDisabled() {
    logButtonDisabled.set(
        client.get().isBlank()
            || project.get().isBlank()
            || task.get().isBlank()
            || notes.get().isBlank());
  }

  void run() {
    loadView();
  }

  void logActivity() {
    model.logActivity(
        clientProperty().get(),
        interval,
        projectProperty().get(),
        taskProperty().get(),
        notesProperty().get());
    loadView();
  }

  private void loadView() {
    updateRecentActivities();
    updateTimeSummary();
  }

  private void updateRecentActivities() {
    var activities = model.selectRecentActivities(31);
    var activityItems = createActivityItems(activities);
    recentActivities.setAll(activityItems);
  }

  private List<ActivityItem> createActivityItems(List<Activity> activities) {
    var date = LocalDate.ofEpochDay(0);
    var rows = new ArrayList<ActivityItem>();
    for (var activity : activities) {
      var dateTime = LocalDateTime.ofInstant(activity.timestamp(), ZoneId.systemDefault());
      if (dateTime.toLocalDate().isAfter(date)) {
        var formattedDate = DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(dateTime);
        rows.add(new ActivityItem(formattedDate));
        date = dateTime.toLocalDate();
      }
      var formattedTime = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(dateTime);
      String text =
          formattedTime
              + " - "
              + activity.project()
              + " ("
              + activity.client()
              + ") "
              + activity.task()
              + " - "
              + activity.notes();
      rows.add(new ActivityItem(text, activity));
    }
    return rows;
  }

  void updateTimeSummary() {
    var timeSummary = model.calculateTimeSummary();
    String timeFormat = "%1$02d:%2$02d";
    hoursToday.set(
        timeFormat.formatted(
            timeSummary.hoursToday().toHours(), timeSummary.hoursToday().toMinutesPart()));
    hoursYesterday.set(
        timeFormat.formatted(
            timeSummary.hoursYesterday().toHours(), timeSummary.hoursYesterday().toMinutesPart()));
    hoursThisWeek.set(
        timeFormat.formatted(
            timeSummary.hoursThisWeek().toHours(), timeSummary.hoursThisWeek().toMinutesPart()));
    hoursThisMonth.set(
        timeFormat.formatted(
            timeSummary.hoursThisMonth().toHours(), timeSummary.hoursThisMonth().toMinutesPart()));
  }

  void setActivity(Activity activity) {
    clientProperty().set(activity.client());
    projectProperty().set(activity.project());
    taskProperty().set(activity.task());
    notesProperty().set(activity.notes());
  }

  void startCountdown(Duration interval) {
    this.interval = interval;
    this.countdown = interval;

    updateProgress();
  }

  void progressCountdown() {
    countdown = countdown.minusSeconds(1);
    if (countdown.isZero()) {
      Optional.ofNullable(onCountdownElapsed).ifPresent(Runnable::run);
      countdown = interval;
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
