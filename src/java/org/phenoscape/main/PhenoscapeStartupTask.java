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
import org.phenoscape.app.CrossPlatform;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.view.CharacterTableComponentFactory;
import org.phenoscape.view.DataSetComponentFactory;
import org.phenoscape.view.MenuFactory;
import org.phenoscape.view.OntologyPreferencesComponentFactory;
import org.phenoscape.view.PhenotypeTableComponentFactory;
import org.phenoscape.view.SessionTermInfoFactory;
import org.phenoscape.view.SpecimenTableComponentFactory;
import org.phenoscape.view.StateTableComponentFactory;
import org.phenoscape.view.TaxonTableComponentFactory;

import phenote.gui.PhenoteDockingTheme;
import phenote.gui.factories.PhenoteOntologyTreeEditorFactory;
import phenote.gui.selection.SelectionBridge;

/**
 * This startup task does all the work of starting the Phenoscape version of Phenote.
 * @author Jim Balhoff
 */
public class PhenoscapeStartupTask extends DefaultGUIStartupTask {
  
  private PhenoscapeController controller;
  
  @Override
  protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
    Collection<GUIComponentFactory<?>> factories = new ArrayList<GUIComponentFactory<?>>();
    factories.add(new DataSetComponentFactory(this.controller));
    factories.add(new CharacterTableComponentFactory(this.controller));
    factories.add(new StateTableComponentFactory(this.controller));
    factories.add(new PhenotypeTableComponentFactory(this.controller));
    factories.add(new TaxonTableComponentFactory(this.controller));
    factories.add(new SpecimenTableComponentFactory(this.controller));
    factories.add(new OntologyPreferencesComponentFactory());
    factories.add(new SessionTermInfoFactory());
    factories.add(new PhenoteOntologyTreeEditorFactory());
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
  protected void configureSystem() {
    super.configureSystem();
    this.controller = new PhenoscapeController();
  }
  
  @Override
  protected String getAppID() {
    return "Phenoscape";
  }
  
  @Override
  protected String getAppName() {
    //return "Phenoscape Annotator";
    return "Phenote";
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
    return "org/phenoscape/view/layouts";
  }

  @Override
  protected String getDefaultPerspectiveResourcePath() {
    if (getPerspectiveResourceDir() != null)
      return getPerspectiveResourceDir() + "/default.idw";
    else
      return null;
  }

  @Override
  public File getPrefsDir() {
    return CrossPlatform.getUserPreferencesFolder(this.getAppID());
  }
  
  @Override
  protected void installSystemListeners() {
    //TODO make dirty document indicator work (and Undo/Redo)
    //GUIManager.addVetoableShutdownListener(DirtyDocumentIndicator.inst());
  }
  
  @Override
  protected void doOtherInstallations() {
    super.doOtherInstallations();
    new SelectionBridge().install();
  }

  @Override
  protected Collection<? extends JMenuItem> getDefaultMenus() {
    return (new MenuFactory(this.controller)).createMenus();
  }
  
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
}
