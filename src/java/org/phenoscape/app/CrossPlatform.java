package org.phenoscape.app;

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

}
