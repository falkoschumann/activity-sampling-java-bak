package de.muspellheim.activitysampling.application;

import java.util.*;

class FakeActivitySamplingView implements ActivitySamplingView {
  private String activityText;
  private boolean logButtonEnabled;
  private List<String> recentActivities;
  private String countdownLabelText;
  private int countdownProgressMaximum;
  private int countdownProgressValue;
  private int showCallCounter;
  private int startCountdownCallCounter;
  private int stopCountdownCallCounter;
  private int countdownElapsedCallCounter;

  @Override
  public void initDelegate(ActivitySamplingViewDelegate delegate) {}

  @Override
  public String getActivityText() {
    return activityText;
  }

  @Override
  public void setActivityText(String activity) {
    activityText = activity;
  }

  @Override
  public boolean isLogButtonEnabled() {
    return logButtonEnabled;
  }

  @Override
  public void setLogButtonEnabled(boolean enabled) {
    logButtonEnabled = enabled;
  }

  @Override
  public List<String> getRecentActivities() {
    return recentActivities;
  }

  @Override
  public void setRecentActivities(List<String> activities) {
    recentActivities = activities;
  }

  @Override
  public String getCountdownLabelText() {
    return countdownLabelText;
  }

  @Override
  public void setCountdownLabelText(String text) {
    countdownLabelText = text;
  }

  @Override
  public int getCountdownProgressMaximum() {
    return countdownProgressMaximum;
  }

  @Override
  public void setCountdownProgressMaximum(int maximum) {
    countdownProgressMaximum = maximum;
  }

  @Override
  public int getCountdownProgressValue() {
    return countdownProgressValue;
  }

  @Override
  public void setCountdownProgressValue(int value) {
    countdownProgressValue = value;
  }

  @Override
  public void show() {
    showCallCounter++;
  }

  int getShowCallCounter() {
    return showCallCounter;
  }

  @Override
  public void startCountdown() {
    startCountdownCallCounter++;
  }

  int getStartCountdownCallCounter() {
    return startCountdownCallCounter;
  }

  @Override
  public void stopCountdown() {
    stopCountdownCallCounter++;
  }

  int getStopCountdownCallCounter() {
    return stopCountdownCallCounter;
  }

  @Override
  public void countdownElapsed() {
    countdownElapsedCallCounter++;
  }

  public int getCountdownElapsedCallCounter() {
    return countdownElapsedCallCounter;
  }
}
