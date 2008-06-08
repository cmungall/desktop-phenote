package org.phenoscape.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.dock.LayoutDriver;
import org.bbop.framework.dock.idw.IDWDriver;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.view.CharacterBrowserComponentFactory;
import org.phenoscape.view.MenuFactory;
import org.phenoscape.view.PhenotypeTableComponentFactory;
import org.phenoscape.view.SpecimenTableComponentFactory;
import org.phenoscape.view.TaxonTableComponentFactory;

import phenote.gui.PhenoteDockingTheme;
import phenote.gui.factories.TermInfoFactory;

public class PhenoscapeStartupTask extends DefaultGUIStartupTask {
  
  private PhenoscapeController controller = new PhenoscapeController();
  
  @Override
  protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
    Collection<GUIComponentFactory<?>> factories = new ArrayList<GUIComponentFactory<?>>();
    factories.add(new CharacterBrowserComponentFactory(this.controller));
    factories.add(new PhenotypeTableComponentFactory(this.controller));
    factories.add(new TaxonTableComponentFactory(this.controller));
    factories.add(new SpecimenTableComponentFactory(this.controller));
    factories.add(new TermInfoFactory());
    return factories;
  }
  
  @Override
  protected void configureLogging() {
    //TODO configure logging properly
    BasicConfigurator.configure();
    final Logger rl = LogManager.getRootLogger();
    rl.setLevel(Level.DEBUG);
  }

  @Override
  protected void configureUI() {
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
  
  @Override
  protected String getAppID() {
    return "phenoscape";
  }
  
  @Override
  protected String getAppName() {
    return "Phenoscape";
  }
  
  @Override
  protected JFrame createFrame() {
    final JFrame frame = super.createFrame();
    frame.setTitle(getAppName());
    return frame;
  }
  
  @Override
  protected LayoutDriver createLayoutDriver() {
    final LayoutDriver driver = super.createLayoutDriver();
    if (driver instanceof IDWDriver) {
      ((IDWDriver)driver).setCustomTheme(new PhenoteDockingTheme());
    }
    driver.setSaveLayoutOnExit(false);
    return driver;
  }
  @Override
  protected String getPerspectiveResourceDir() {
    return "phenote/gui/layout/resources";
  }

  @Override
  protected String getDefaultPerspectiveResourcePath() {
    if (getPerspectiveResourceDir() != null)
      return getPerspectiveResourceDir() + "/phenote_classic.idw";
    else
      return null;
  }

  @Override
  protected File getPrefsDir() {
    //TODO should be platform specific
    return new File(System.getProperty("user.home") + "/.phenote");
  }
  
  @Override
  protected void installSystemListeners() {
    //TODO make dirty document indicator work
    //GUIManager.addVetoableShutdownListener(DirtyDocumentIndicator.inst());
  }
  
  @Override
  protected Collection<? extends JMenuItem> getDefaultMenus() {
    return (new MenuFactory(this.controller)).createMenus();
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
