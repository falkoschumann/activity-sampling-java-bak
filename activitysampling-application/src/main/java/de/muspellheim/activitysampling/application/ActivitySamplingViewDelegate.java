package de.muspellheim.activitysampling.application;

import java.time.*;

public interface ActivitySamplingViewDelegate {
  void activityTextChanged();

  void logActivity();

  void startCountdown(Duration interval);

  void stopCountdown();

  void progressCountdown();
}
