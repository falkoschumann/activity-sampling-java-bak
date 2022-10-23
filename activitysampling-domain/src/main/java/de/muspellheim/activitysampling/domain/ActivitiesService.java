package de.muspellheim.activitysampling.domain;

import java.util.*;

public interface ActivitiesService {
  void logActivity(String description);

  List<Activity> selectAllActivities();
}
