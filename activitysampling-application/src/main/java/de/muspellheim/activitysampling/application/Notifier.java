package de.muspellheim.activitysampling.application;

import java.awt.*;

class Notifier {
  private TrayIcon trayIcon;

  Notifier() {
    if (SystemTray.isSupported()) {
      EventQueue.invokeLater(
          () -> {
            // Mac: 20x20
            var trayIconSize = SystemTray.getSystemTray().getTrayIconSize();
            var isRetina =
                !GraphicsEnvironment.getLocalGraphicsEnvironment()
                    .getDefaultScreenDevice()
                    .getDefaultConfiguration()
                    .getDefaultTransform()
                    .isIdentity();
            System.out.println("Tray icon size: " + trayIconSize + ", is retina: " + isRetina);
            String imageUrl;
            if (trayIconSize.height == 16) {
              // Windows 10
              imageUrl = isRetina ? "/icons/punch-clock-32.png" : "/icons/punch-clock-16.png";
            } else if (trayIconSize.height == 20) {
              // macOS 12
              imageUrl = isRetina ? "/icons/punch-clock-40.png" : "/icons/punch-clock-20.png";
            } else {
              // Other OS
              imageUrl = "/icons/punch-clock-16.png";
            }
            var url = getClass().getResource(imageUrl);
            var image = Toolkit.getDefaultToolkit().getImage(url);
            var tray = SystemTray.getSystemTray();
            trayIcon = new TrayIcon(image, "Activity Sampling");
            try {
              tray.add(trayIcon);
            } catch (AWTException e) {
              throw new RuntimeException(e);
            }
          });
    } else {
      System.err.println("System tray not supported.");
    }
  }

  void displayNotification() {
    if (trayIcon == null) {
      return;
    }

    EventQueue.invokeLater(
        () -> trayIcon.displayMessage("What are you working on?", null, TrayIcon.MessageType.INFO));
  }

  void dispose() {
    if (trayIcon == null) {
      return;
    }

    EventQueue.invokeLater(
        () -> {
          var tray = SystemTray.getSystemTray();
          tray.remove(trayIcon);
        });
  }
}
