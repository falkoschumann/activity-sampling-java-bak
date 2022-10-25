package de.muspellheim.activitysampling.application;

import de.muspellheim.activitysampling.domain.*;
import de.muspellheim.activitysampling.infrastructure.*;
import java.nio.file.*;
import javafx.application.*;
import javafx.stage.*;

public class App extends Application {
  private ActivitiesServiceImpl activitiesService;

  @Override
  public void init() {
    var file = Paths.get(System.getProperty("user.home"), "activity-log.csv");
    var eventStore = new CsvEventStore(file);
    activitiesService = new ActivitiesServiceImpl(eventStore);
  }

  @Override
  public void start(Stage primaryStage) {
    var controller = ActivitySamplingView.create(primaryStage, activitiesService);
    controller.run();
  }
}
