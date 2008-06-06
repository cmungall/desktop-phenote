package org.phenoscape.main;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.ViewMenu;
import org.bbop.framework.dock.LayoutDriver;
import org.bbop.framework.dock.idw.IDWDriver;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;
import org.phenoscape.model.PhenoscapeController;
import org.phenoscape.view.CharacterBrowserComponentFactory;
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
  protected void configureUI() {
    // overriding the odd colors in BBOP framework by doing nothing here
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
    //GUIManager.addVetoableShutdownListener(DirtyDocumentIndicator.inst());
  }
  
  @Override
  protected Collection<? extends JMenuItem> getDefaultMenus() {
    Collection<JMenuItem> menus = new ArrayList<JMenuItem>();
    menus.add(this.createFileMenu());
    menus.add(new ViewMenu());
    return menus;
  }
  
  private JMenuItem createFileMenu() {
    final JMenu menu = new JMenu("File");
    final Action openAction = new AbstractAction("Open...") {
      public void actionPerformed(ActionEvent e) { controller.open(); }
      };
    openAction.putValue(Action.ACCELERATOR_KEY, this.getKeyStroke(KeyEvent.VK_O));
    menu.add(new JMenuItem(openAction));
    return menu;
  }
  
  private KeyStroke getKeyStroke(int keyCode) {
    return KeyStroke.getKeyStroke(keyCode, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
  }
  
}
