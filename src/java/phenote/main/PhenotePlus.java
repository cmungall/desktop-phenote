package phenote.main;

import javax.swing.SwingUtilities;

import org.bbop.framework.GUIManager;

public class PhenotePlus {
  
  private static String[] args;

  public static void main(String[] args) {
    PhenotePlus.args = args;
    Runnable r = new PhenoteRunnable();
    // Set application name on Mac so it doesn't use the ugly Java class name
    System.setProperty("com.apple.mrj.application.apple.menu.about.name", getAppName());
//      System.out.println("about to show splash screen"); // DEL
    Phenote.showBePatientDialog();
//      Phenote.getPhenote().splashScreenInit();  // can't call statically
    SwingUtilities.invokeLater(r);
  }

  private static class PhenoteRunnable implements Runnable {
    public void run() {
      GUIManager.getManager().addStartupTask(new PhenoteStartupTask(args));
      GUIManager.getManager().start();
    }
  }

  public static String getAppName() {
    return "Phenote";
  }
}
