package org.phenoscape.main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;

public class Phenoscape {

  public static void main(String[] args) {
    configureLogging();
    configureLookAndFeel();
    SwingUtilities.invokeLater(new PhenoscapeRunnable());
  }
  
  private static class PhenoscapeRunnable implements Runnable {
    public void run() {
      GUIManager.getManager().addStartupTask(new PhenoscapeStartupTask());
      GUIManager.getManager().start();
    }
  }
  
  private static void configureLogging() {
    //TODO configure logging properly
    BasicConfigurator.configure();
    final Logger rl = LogManager.getRootLogger();
    rl.setLevel(Level.DEBUG);
  }

  private static void configureLookAndFeel() {
    try {
      final String lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
      if (lookAndFeelClassName.equals("apple.laf.AquaLookAndFeel")) {
        // We are running on Mac OS X - use the Quaqua look and feel
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
      } else {
        // We are on some other platform, use the system look and feel
        UIManager.setLookAndFeel(lookAndFeelClassName);
      }
    } catch (ClassNotFoundException e) {
      log().error("Look and feel class not found", e);
    } catch (InstantiationException e) {
      log().error("Could not instantiate look and feel", e);
    } catch (IllegalAccessException e) {
      log().error("Error setting look and feel", e);
    } catch (UnsupportedLookAndFeelException e) {
      log().error("Look and feel not supported", e);
    }
  }
    
    private static Logger log() {
      return Logger.getLogger(Phenoscape.class);
    }
}
