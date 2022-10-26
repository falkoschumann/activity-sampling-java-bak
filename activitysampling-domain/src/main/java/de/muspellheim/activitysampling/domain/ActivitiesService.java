package de.muspellheim.activitysampling.domain;

import java.time.*;
import java.util.*;

public interface ActivitiesService {
  void logActivity(String client, Duration duration, String project, String task, String notes);

  List<Activity> selectAllActivities();
}
