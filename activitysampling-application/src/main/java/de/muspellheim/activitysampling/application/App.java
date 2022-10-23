package de.muspellheim.activitysampling.application;

import de.muspellheim.activitysampling.domain.*;
import de.muspellheim.activitysampling.infrastructure.*;
import java.nio.file.*;
import javax.swing.*;

public class App {
  public static void main(String[] args) {
    var file = Paths.get(System.getProperty("user.home"), "activity-sampling.csv");
    var eventStore = new CsvEventStore(file);
    var model = new ActivitiesServiceImpl(eventStore);
    SwingUtilities.invokeLater(
        () -> {
          var view = new SwingActivitySamplingView();
          var controller = new ActivitySamplingController(model, view);
          controller.run();
        });
  }
}
