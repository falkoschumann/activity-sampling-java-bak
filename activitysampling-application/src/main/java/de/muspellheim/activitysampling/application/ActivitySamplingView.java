package de.muspellheim.activitysampling.application;

import java.util.*;

public interface ActivitySamplingView {
  void initDelegate(ActivitySamplingViewDelegate delegate);

  String getActivityText();

  void setActivityText(String text);

  boolean isLogButtonEnabled();

  void setLogButtonEnabled(boolean enabled);

  List<String> getRecentActivities();

  void setRecentActivities(List<String> activities);

  String getCountdownLabelText();

  void setCountdownLabelText(String text);

  int getCountdownProgressMaximum();

  void setCountdownProgressMaximum(int maximum);

  int getCountdownProgressValue();

  void setCountdownProgressValue(int value);

  void show();

  void startCountdown();

  void stopCountdown();

  void countdownElapsed();
}
