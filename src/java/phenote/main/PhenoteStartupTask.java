package phenote.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIManager;
import org.bbop.framework.GUITask;
import org.bbop.framework.ScreenLockTask;
import org.bbop.framework.ViewMenu;
import org.bbop.framework.dock.LayoutDriver;
import org.bbop.framework.dock.idw.IDWDriver;
import org.bbop.util.CollectionUtil;
import org.oboedit.example.OBDAnnotationNumberFetchBehaviorTask;
import org.oboedit.gui.Preferences;
import org.oboedit.gui.factory.AnnotationSummaryComponentFactory;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;

import phenote.charactertemplate.CharacterTemplateTableFactory;
import phenote.charactertemplate.TemplateChooserFactory;
import phenote.config.Config;
import phenote.config.xml.GroupDocument.Group;
import phenote.config.xml.TemplatechooserDocument.Templatechooser;
import phenote.edit.DirtyDocumentIndicator;
import phenote.gui.PhenoteDockingTheme;
import phenote.gui.PhenoteMainFrame;
import phenote.gui.StandardToolbar;
import phenote.gui.actions.AboutAction;
import phenote.gui.factories.CharacterTableFactory;
import phenote.gui.factories.NCBIInfoFactory;
import phenote.gui.factories.PhenoteEditorFactory;
import phenote.gui.factories.PhenoteGraphViewFactory;
import phenote.gui.factories.PhenoteOntologyTreeEditorFactory;
import phenote.gui.factories.PhenoteTreeViewFactory;
import phenote.gui.factories.ProtocolEditorFactory;
import phenote.gui.factories.ScratchGroupsViewFactory;
import phenote.gui.factories.TermInfoFactory;
import phenote.gui.menu.EditMenu;
import phenote.gui.menu.FileMenu;
import phenote.gui.menu.PhenoteHelpMenu;
import phenote.gui.menu.SettingsMenu;
import phenote.gui.selection.SelectionBridge;

public class PhenoteStartupTask extends DefaultGUIStartupTask {

  private Logger LOG = Logger.getLogger(PhenoteStartupTask.class);
  private String[] args;
  private SelectionBridge selectionBridge = new SelectionBridge();

  PhenoteStartupTask(String[] args) {
    this.args = args;
    initPhenote();
  }

  @Override
  protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
    // Collection<GUIComponentFactory<?>> factories =
    // super.getDefaultComponentFactories();
    Collection<GUIComponentFactory<?>> factories = new ArrayList<GUIComponentFactory<?>>();
//  factories.add(new FieldPanelFactory());
    factories.add(new PhenoteEditorFactory());
    factories.add(new TermInfoFactory());
    factories.add(new NCBIInfoFactory());
    factories.addAll(this.getCharacterTableComponentFactories());
//  factories.add(new GraphEditorFactory());  <-- removing this for upcoming 1.5 release
    factories.add(new PhenoteTreeViewFactory());
    factories.add(new PhenoteGraphViewFactory());
    factories.add(new PhenoteOntologyTreeEditorFactory());
    factories.add(new AnnotationSummaryComponentFactory());
    factories.addAll(this.getTemplateGroupComponentFactories());
    factories.add(new ProtocolEditorFactory());
    factories.add(new ScratchGroupsViewFactory());
    return factories;
  }

  /** this is called at initialization i believe */
  @Override
  protected void doOtherInstallations() {
  }

  /** perhaps this might change in the future so both oboedit and phenote will
   *  use the same default toolbars with save, exit, etc. buttons
   */
  @Override
  protected Collection<JToolBar> getDefaultToolBars() {
    LOG.debug("getting Default Toolbars");
    Collection<JToolBar> toolbars = new ArrayList<JToolBar>();
    GUIComponent tb = new StandardToolbar();
    toolbars.add((JToolBar) tb.getComponent());
    return toolbars;
    // new Throwable().printStackTrace();
  }


  private void initPhenote() {
    Phenote.initBackend(args);
    selectionBridge.install();
    // init backend sets off splash screent that must be destroyed
    Phenote.getPhenote().splashScreenDestruct();
  }

  @Override
  protected String getAppID() {
    return "phenote";
  }

  @Override
  protected String getAppName() {
    return Phenote2.getAppName();
  }

  protected Action getAboutAction() {
    return new AboutAction();
  }

  /** in this directory obo/idw expects to find a file named perspectives.xml
      which then points to idw files in this same directory 
      obo can get this from a jar but doesnt work from webstart jar???
      FileNotFoundException printed to stdout if not found and no perspectives
      come up */
  @Override
  protected String getPerspectiveResourceDir() { // not using org
    return "phenote/gui/layout/resources"; // need to add this
    // "org/phenote/gui/layout/resources";
    // return System.getProperty("user.home") + "/.phenote";
  }

  @Override
  protected String getDefaultPerspectiveResourcePath() {
    if (getPerspectiveResourceDir() != null)
      return getPerspectiveResourceDir() + "/phenote_classic.idw";
    else
      return null;
  }

  @Override
  /** im not sure about this and svn & webstart?
   so idw/obo adds a perspective subdir here and copies perspectives here
   from jars/classes */
  protected File getPrefsDir() {
    // .phenote/layout?
    return new File(System.getProperty("user.home") + "/.phenote");
  }

  @Override
  protected Collection<? extends JMenuItem> getDefaultMenus() {
    Collection<JMenuItem> menus = new ArrayList<JMenuItem>();
    // add any other Phenote menus
    menus.add(new FileMenu());
    menus.add(new EditMenu());
    menus.add(new ViewMenu());
    menus.add(new SettingsMenu());
    menus.add(new PhenoteHelpMenu());
    return menus;
  }

  @Override
  protected Collection<GUITask> getDefaultTasks() {
    ScreenLockTask screenLockTask = new ScreenLockTask(GUIManager
        .getManager().getScreenLockQueue(), GUIManager.getManager()
        .getFrame(), Preferences.getPreferences()
        .getUseModalProgressMonitors());
    return CollectionUtil.list((GUITask) screenLockTask,
        new OBDAnnotationNumberFetchBehaviorTask()); // TODO: for
    // testing
  }

  @Override
  protected void installSystemListeners() {
    GUIManager.addVetoableShutdownListener(new DirtyDocumentIndicator());
  }

  @Override
  protected JFrame createFrame() {
    JFrame out = new PhenoteMainFrame(getAppID());
    out.setTitle(getAppName());
    return out;
  }

  @Override
  protected void configureUI() {
    // overriding the odd colors in BBOP framework
  }
  
  

  @Override
  protected LayoutDriver createLayoutDriver() {
    final LayoutDriver driver = super.createLayoutDriver();
    if (driver instanceof IDWDriver) {
      ((IDWDriver)driver).setCustomTheme(new PhenoteDockingTheme());
    }
    return driver;
  }

  /**
   * Create a character table factory configured for each group in config
   */
  private Collection<CharacterTableFactory> getCharacterTableComponentFactories() {
    Collection<CharacterTableFactory> factories = new ArrayList<CharacterTableFactory>();
    for (Group group : Config.inst().getFieldGroups()) {
      log().debug("Creating table factory for: " + group.getName());
      if (!group.getInterface().equals(Group.Interface.CHARACTER_TEMPLATE)) {
        factories.add(new CharacterTableFactory(group.getName()));
      }
    }
    if (factories.size() < 1) {
      // no groups have been configured, so just do default
      factories.add(new CharacterTableFactory(this.getDefaultGroup()));
    }
    return factories;
  }

  private Collection<GUIComponentFactory<?>> getTemplateGroupComponentFactories() {
    Collection<GUIComponentFactory<?>> factories = new ArrayList<GUIComponentFactory<?>>();
    for (Group group : Config.inst().getFieldGroups()) {
      if (group.getInterface().equals(Group.Interface.CHARACTER_TEMPLATE)) {
        factories.add(new CharacterTemplateTableFactory(group.getName(), group.getGroupAdapter()));
        for (Templatechooser chooserConfig : group.getTemplatechooserArray()) {
          factories.add(new TemplateChooserFactory(group.getName(), chooserConfig.getAdapter(), chooserConfig.getTitle(), chooserConfig.getField()));
        }
      }
    }
    return factories;
  }

  private String getDefaultGroup() {
    return Config.inst().getDefaultGroup().getName();
  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
}
