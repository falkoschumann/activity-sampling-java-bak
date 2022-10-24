package de.muspellheim.activitysampling.application;

import de.muspellheim.activitysampling.domain.*;
import de.muspellheim.activitysampling.infrastructure.*;
import java.nio.file.*;
import javafx.application.*;
import javafx.stage.*;

public class App extends Application {
  private ActivitiesServiceImpl activitiesService;
  /*
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
  */

  @Override
  public void init() throws Exception {
    var file = Paths.get(System.getProperty("user.home"), "activity-sampling.csv");
    var eventStore = new CsvEventStore(file);
    activitiesService = new ActivitiesServiceImpl(eventStore);
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    var controller = ActivitySamplingView.create(primaryStage, activitiesService);
    controller.run();
  }
}
