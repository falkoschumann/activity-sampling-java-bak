package de.muspellheim.activitysampling.application;

import de.muspellheim.activitysampling.domain.*;
import java.time.Duration;
import java.util.*;
import javafx.application.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.stage.*;

public class ActivitySamplingView {
  @FXML private Stage stage;
  @FXML private MenuBar menuBar;
  @FXML private TextField clientText;
  @FXML private TextField projectText;
  @FXML private ComboBox<String> taskCombo;
  @FXML private TextField notesText;
  @FXML private Button logButton;
  @FXML private Label countdownLabel;
  @FXML private ProgressBar countdownProgress;
  @FXML private ListView<ActivityItem> recentActivities;

  private final ActivitySamplingViewModel viewModel;
  private final Notifier notifier;
  private final Timer timer = new Timer("System clock", true);

  private CountdownTask countdownTask;

  public ActivitySamplingView(ActivitiesService model) {
    viewModel = new ActivitySamplingViewModel(model);
    notifier = new Notifier();
    viewModel.onCountdownElapsed = notifier::displayNotification;
  }

  public static ActivitySamplingView create(Stage stage, ActivitiesService model) {
    String file = "/fxml/ActivitySamplingView.fxml";
    try {
      var url = ActivitySamplingView.class.getResource(file);
      var loader = new FXMLLoader(url);
      loader.setRoot(stage);
      loader.setControllerFactory(type -> new ActivitySamplingView(model));
      loader.load();
      return loader.getController();
    } catch (Exception e) {
      throw new IllegalStateException("Could not load view: " + file, e);
    }
  }

  @FXML
  private void initialize() {
    stage
        .getIcons()
        .addAll(
            new Image(
                Objects.requireNonNull(
                    getClass().getResourceAsStream("/icons/punch-clock-16.png"))),
            new Image(
                Objects.requireNonNull(
                    getClass().getResourceAsStream("/icons/punch-clock-32.png"))));
    menuBar.setUseSystemMenuBar(true);
    taskCombo
        .getItems()
        .addAll(
            "Analyze",
            "Deployment",
            "Design",
            "Documentation",
            "Maintenance",
            "Meeting",
            "Planing",
            "Programming",
            "Rework",
            "Testing");

    stage.setOnCloseRequest(e -> notifier.dispose());
    clientText.textProperty().bindBidirectional(viewModel.clientProperty());
    projectText.textProperty().bindBidirectional(viewModel.projectProperty());
    taskCombo.valueProperty().bindBidirectional(viewModel.taskProperty());
    notesText.textProperty().bindBidirectional(viewModel.notesProperty());
    logButton.disableProperty().bind(viewModel.logButtonDisabledProperty());
    countdownLabel.textProperty().bind(viewModel.countdownTextProperty());
    countdownProgress.progressProperty().bind(viewModel.countdownProgressProperty());
    recentActivities.setItems(viewModel.getRecentActivities());
    recentActivities.setCellFactory(view -> new ActivityListCell(viewModel::setActivity));
  }

  public void run() {
    stage.show();
    viewModel.run();
    scrollRecentActivitiesToEnd();
  }

  @FXML
  private void handleExit() {
    Platform.exit();
  }

  @FXML
  private void handleStart5Min() {
    startCountdown(Duration.ofMinutes(5));
  }

  @FXML
  private void handleStart10Min() {
    startCountdown(Duration.ofMinutes(10));
  }

  @FXML
  private void handleStart15Min() {
    startCountdown(Duration.ofMinutes(15));
  }

  @FXML
  private void handleStart20Min() {
    startCountdown(Duration.ofMinutes(20));
  }

  @FXML
  private void handleStart30Min() {
    startCountdown(Duration.ofMinutes(30));
  }

  @FXML
  private void handleStart60Min() {
    startCountdown(Duration.ofMinutes(60));
  }

  @FXML
  private void handleStart1Min() {
    startCountdown(Duration.ofMinutes(1));
  }

  private void startCountdown(Duration interval) {
    stopCountdown();
    viewModel.startCountdown(interval);
    countdownTask = new CountdownTask();
    timer.scheduleAtFixedRate(countdownTask, 0, 1000);
  }

  @FXML
  private void handleStop() {
    stopCountdown();
  }

  private void stopCountdown() {
    Optional.ofNullable(countdownTask).ifPresent(TimerTask::cancel);
  }

  @FXML
  private void handleLog() {
    viewModel.logActivity();
    scrollRecentActivitiesToEnd();
  }

  private void scrollRecentActivitiesToEnd() {
    recentActivities.scrollTo(viewModel.getRecentActivities().size() - 1);
  }

  private class CountdownTask extends TimerTask {
    @Override
    public void run() {
      Platform.runLater(viewModel::progressCountdown);
    }
  }
}
