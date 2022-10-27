package de.muspellheim.activitysampling.domain;

import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

public class ActivitiesServiceImpl implements ActivitiesService {
  private final EventStore eventStore;
  private final Clock clock;

  public ActivitiesServiceImpl(EventStore eventStore) {
    this(eventStore, Clock.systemDefaultZone());
  }

  public ActivitiesServiceImpl(EventStore eventStore, Clock clock) {
    this.eventStore = eventStore;
    this.clock = clock;
  }

  @Override
  public void logActivity(
      String client, Duration duration, String project, String task, String notes) {
    ActivityLoggedEvent event =
        new ActivityLoggedEvent(Instant.now(clock), duration, client, project, task, notes);
    eventStore.record(event);
  }

  @Override
  public List<Activity> selectAllActivities() {
    return StreamSupport.stream(eventStore.replay().spliterator(), false)
        .filter((event -> event instanceof ActivityLoggedEvent))
        .map(event -> (ActivityLoggedEvent) event)
        .map(
            event ->
                new Activity(
                    event.timestamp(),
                    event.duration(),
                    event.client(),
                    event.project(),
                    event.task(),
                    event.notes()))
        .toList();
  }

  @Override
  public List<Activity> selectRecentActivities(int numberOfDays) {
    var today = LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault());
    var checkpoint = today.minusDays(numberOfDays);
    return selectAllActivities().stream()
        .filter(
            activity -> {
              var date = LocalDate.ofInstant(activity.timestamp(), ZoneId.systemDefault());
              return date.isAfter(checkpoint);
            })
        .toList();
  }

  @Override
  public TimeSummary calculateTimeSummary() {
    var hoursToday = Duration.ZERO;
    var hoursYesterday = Duration.ZERO;
    var hoursThisWeek = Duration.ZERO;
    var hoursThisMonth = Duration.ZERO;
    var today = LocalDate.ofInstant(clock.instant(), ZoneId.systemDefault());
    var startOfMonth = today.withDayOfMonth(1);
    for (Activity activity : selectAllActivities()) {
      var date = LocalDate.ofInstant(activity.timestamp(), ZoneId.systemDefault());
      if (date.isBefore(startOfMonth)) {
        continue;
      }

      if (date.equals(today)) {
        hoursToday = hoursToday.plus(activity.duration());
      } else if (date.equals(today.minusDays(1))) {
        hoursYesterday = hoursYesterday.plus(activity.duration());
      }
      if (date.get(ChronoField.ALIGNED_WEEK_OF_YEAR)
          == today.get(ChronoField.ALIGNED_WEEK_OF_YEAR)) {
        hoursThisWeek = hoursThisWeek.plus(activity.duration());
      }
      hoursThisMonth = hoursThisMonth.plus(activity.duration());
    }
    return new TimeSummary(hoursToday, hoursYesterday, hoursThisWeek, hoursThisMonth);
  }
}
