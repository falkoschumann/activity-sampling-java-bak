package de.muspellheim.activitysampling.application;

import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

class SwingActivitySamplingView extends JFrame implements ActivitySamplingView {
  static {
    System.setProperty("apple.laf.useScreenMenuBar", "true");
  }

  private final JTextField activityText;
  private final JButton logButton;
  private final DefaultListModel<String> recentActivitiesListModel;
  private final JLabel countdownLabel;
  private final JProgressBar countdownProgress;
  private final Timer timer;
  private TrayIcon trayIcon;
  private ActivitySamplingViewDelegate delegate;

  SwingActivitySamplingView() {
    setTitle("Activity Sampling");
    setLocationByPlatform(true);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    var menuBar = new JMenuBar();
    var fileMenu = new JMenu("File");
    var exitMenuItem = new JMenuItem("Exit");
    exitMenuItem.addActionListener(
        e -> {
          dispose();
          System.exit(0);
        });
    fileMenu.add(exitMenuItem);
    menuBar.add(fileMenu);
    var notificationsMenu = new JMenu("Notifications");
    var startMenu = new JMenu("Start");
    addInterval(startMenu, 5);
    addInterval(startMenu, 10);
    addInterval(startMenu, 15);
    addInterval(startMenu, 20);
    addInterval(startMenu, 30);
    addInterval(startMenu, 60);
    addInterval(startMenu, 1);
    notificationsMenu.add(startMenu);
    var stopMenuItem = new JMenuItem("Stop");
    stopMenuItem.addActionListener(e -> delegate.stopCountdown());
    notificationsMenu.add(stopMenuItem);
    menuBar.add(notificationsMenu);
    setJMenuBar(menuBar);

    if (SystemTray.isSupported()) {
      EventQueue.invokeLater(
          () -> {
            var isRetina =
                !GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getDefaultTransform()
                    .isIdentity();
            var imageUrl = isRetina ? "/icons/punch-clock@2.png" : "/icons/punch-clock.png";
            var url = getClass().getResource(imageUrl);
            var image = Toolkit.getDefaultToolkit().getImage(url);
            var tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(image, "Activity Sampling");
            trayIcon.addActionListener(e -> System.out.println("tray action"));
            try {
              tray.add(trayIcon);
              System.out.println("Tray icon added");
            } catch (AWTException e) {
              throw new RuntimeException(e);
            }
          });
    } else {
      System.err.println("System tray not supported.");
    }

    var content = new JPanel(new GridBagLayout());
    content.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
    add(content);

    var activityLabel = new JLabel("Activity:");
    var constraints = new GridBagConstraints();
    constraints.gridy = 0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    content.add(activityLabel, constraints);

    activityText = new JTextField();
    activityText.addKeyListener(
        new KeyAdapter() {
          @Override
          public void keyReleased(KeyEvent e) {
            if (e.getKeyChar() == KeyEvent.VK_ENTER) {
              delegate.logActivity();
            } else {
              delegate.activityTextChanged();
            }
          }
        });
    constraints = new GridBagConstraints();
    constraints.gridy = 1;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    content.add(activityText, constraints);

    logButton = new JButton("Log");
    constraints = new GridBagConstraints();
    constraints.gridy = 2;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    content.add(logButton, constraints);

    countdownLabel = new JLabel("00:20:00");
    constraints = new GridBagConstraints();
    constraints.gridy = 3;
    content.add(countdownLabel, constraints);

    countdownProgress = new JProgressBar();
    countdownProgress.setMaximum(60);
    countdownProgress.setValue(0);
    constraints = new GridBagConstraints();
    constraints.gridy = 4;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    content.add(countdownProgress, constraints);

    timer = new Timer((int) Duration.ofSeconds(1).toMillis(), e -> delegate.progressCountdown());

    recentActivitiesListModel = new DefaultListModel<>();
    var recentActivitiesList = new JList<>(recentActivitiesListModel);
    var activityScroll = new JScrollPane(recentActivitiesList);
    activityScroll.setPreferredSize(new Dimension(300, 400));
    constraints = new GridBagConstraints();
    constraints.gridy = 5;
    content.add(activityScroll, constraints);

    pack();
  }

  private void addInterval(JMenu menu, int minutes) {
    var menuItem = new JMenuItem(minutes + " min");
    menuItem.addActionListener(e -> delegate.startCountdown(Duration.ofMinutes(minutes)));
    menu.add(menuItem);
  }

  @Override
  public void initDelegate(ActivitySamplingViewDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public String getActivityText() {
    return activityText.getText();
  }

  @Override
  public void setActivityText(String text) {
    activityText.setText(text);
  }

  @Override
  public boolean isLogButtonEnabled() {
    return logButton.isEnabled();
  }

  @Override
  public void setLogButtonEnabled(boolean enabled) {
    logButton.setEnabled(enabled);
  }

  @Override
  public List<String> getRecentActivities() {
    return Collections.list(recentActivitiesListModel.elements());
  }

  @Override
  public void setRecentActivities(List<String> activities) {
    recentActivitiesListModel.clear();
    recentActivitiesListModel.addAll(activities);
  }

  @Override
  public String getCountdownLabelText() {
    return countdownLabel.getText();
  }

  @Override
  public void setCountdownLabelText(String text) {
    countdownLabel.setText(text);
  }

  @Override
  public int getCountdownProgressMaximum() {
    return countdownProgress.getMaximum();
  }

  @Override
  public void setCountdownProgressMaximum(int maximum) {
    countdownProgress.setMaximum(maximum);
  }

  @Override
  public int getCountdownProgressValue() {
    return countdownProgress.getValue();
  }

  @Override
  public void setCountdownProgressValue(int value) {
    countdownProgress.setValue(value);
  }

  @Override
  public void startCountdown() {
    timer.start();
  }

  @Override
  public void stopCountdown() {
    timer.stop();
  }

  @Override
  public void countdownElapsed() {
    EventQueue.invokeLater(
        () -> trayIcon.displayMessage("What are you working on?", null, TrayIcon.MessageType.INFO));
  }
}
