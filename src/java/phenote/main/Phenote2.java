package phenote.main;

import javax.swing.SwingUtilities;

import org.bbop.framework.GUIManager;

public class Phenote2 {
	public static void main(String[] args) {
		Runnable r = new PhenoteRunnable();
		SwingUtilities.invokeLater(r);
	}

  private static class PhenoteRunnable implements Runnable {
    public void run() {
      GUIManager.getManager().addStartupTask(new PhenoteStartupTask());
      GUIManager.getManager().start();
    }
  }
}
