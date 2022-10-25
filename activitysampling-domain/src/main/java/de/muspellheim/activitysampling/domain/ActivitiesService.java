package de.muspellheim.activitysampling.domain;

import java.util.*;

public interface ActivitiesService {
  void logActivity(String client, String project, String task, String notes);

  List<Activity> selectAllActivities();
}
