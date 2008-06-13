package org.phenoscape.app;

import java.io.File;

public class CrossPlatform {
  
  public static enum Platform { MAC, WINDOWS, UNIX; }
  
  public static Platform getCurrentPlatform() {
    final String name = System.getProperty("os.name");
    if (name.startsWith("Mac")) {
      return Platform.MAC;
    } else if (name.startsWith("Windows")) {
      return Platform.WINDOWS;
    } else {
      return Platform.UNIX;
    }
  }
  
  /**
   * We don't want to add "Exit" to the File menu on Mac.
   * Instead there is "Quit" under the automatic app menu.
   */
  public static boolean shouldPutExitInFileMenu() {
    return !getCurrentPlatform().equals(Platform.MAC);
  }
  
  public static File getUserPreferencesFolder(String name) {
    final String homePath = System.getProperty("user.home");
    switch(getCurrentPlatform()) {
    // it would be much better to find a supported method for obtaining the Application Support folder
    case MAC: return new File(homePath, "Library/Application Support/" + name);
    case WINDOWS: return new File(homePath, name);
      // UNIX behavior is default
    default: return new File(homePath, "." + name.toLowerCase());
    }
  }

}
