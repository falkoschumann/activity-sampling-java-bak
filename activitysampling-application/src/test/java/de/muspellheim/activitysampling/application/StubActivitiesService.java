package de.muspellheim.activitysampling.application;

import de.muspellheim.activitysampling.domain.*;
import java.time.*;
import java.util.*;

class StubActivitiesService implements ActivitiesService {
  private final Clock clock;
  private List<Activity> allActivities = new ArrayList<>();

  StubActivitiesService(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void logActivity(String client, String project, String task, String notes) {
    var activity = new Activity(clock.instant(), client, project, task, notes);
    allActivities.add(activity);
  }

  @Override
  public List<Activity> selectAllActivities() {
    return allActivities;
  }

  public void setAllActivities(List<Activity> activities) {
    allActivities = activities;
  }
}
