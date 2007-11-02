package phenote.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.awt.Dimension;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIComponentWrapper;
import org.bbop.framework.ViewMenu;
import org.bbop.util.CollectionUtil;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;

import phenote.config.Config;
import phenote.gui.CharacterTableController;
import phenote.gui.TermInfo2;
import phenote.gui.field.FieldPanel;

public class PhenoteStartupTask extends DefaultGUIStartupTask {

  private Logger LOG =  Logger.getLogger(PhenoteStartupTask.class);
  private String[] args;

  PhenoteStartupTask(String[] args) {
    this.args = args;
  }

	@Override
	protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
		//Collection<GUIComponentFactory<?>> factories = super.getDefaultComponentFactories();
    Collection<GUIComponentFactory<?>> factories =
      new ArrayList<GUIComponentFactory<?>>();
		factories.add(new FieldPanelFactory());
    factories.add(new TermInfoFactory());
    factories.add(new CharTableFactory());
		return factories;
	}

  /** this is called at initialization i believe */
	@Override
	protected void doOtherInstallations() {
    LOG.debug("doOtherInstallations called");
    //new Throwable().printStackTrace();
    initPhenote();
  }
	
  private void initPhenote() {
    Phenote.initBackend(args);
    // init backend sets off splash screent that must be destroyed
    Phenote.getPhenote().splashScreenDestruct();
  }

	@Override
	protected String getAppID() {
		return "phenote";
	}
	
	@Override
	protected String getAppName() {
		return "Phenote";
	}

	@Override
  // ??
	protected String getPerspectiveResourceDir() { // not using org
		return "phenote/gui/layout/resource"; // need to add this
    //"org/phenote/gui/layout/resources";
    //return System.getProperty("user.home") + "/.phenote";
	}

	@Override
  /** im not sure about this and svn & webstart? */
	protected File getPrefsDir() {
    // .phenote/layout?
		return new File(System.getProperty("user.home") + "/.phenote");
	}

	@Override
	protected Collection<? extends JMenuItem> getDefaultMenus() {
		Collection<JMenuItem> menus = new ArrayList<JMenuItem>();
		// add any other Phenote menus
		menus.add(new ViewMenu());
		return menus;
	}


  /** FieldPanelFactory inner class */
  private class FieldPanelFactory implements GUIComponentFactory {
    public GUIComponent createComponent(String id) {
      String l = "<html><h1>This is an example component</h1></html>";
      //if (true) return new GUIComponentWrapper(id, id, new javax.swing.JLabel(l));
      // for now just doing default group - need to iterate groups
      FieldPanel groupFieldPanel = new FieldPanel(); //true,false,Config.inst().getDefaultGroup(), this.tableController.getSelectionModel());
      groupFieldPanel.setMinimumSize(new Dimension(300,300));
      groupFieldPanel.setPreferredSize(new Dimension(300,300));
      // 1st is id, 2nd id -> title bar string
      return new GUIComponentWrapper(id, id, groupFieldPanel);
    }

    public FactoryCategory getCategory() { return FactoryCategory.ANNOTATION; }

    public String getDefaultID() { return "dnote-editor"; }
    /** These are alias IDs? */
    public List getIDs() { return CollectionUtil.list("dnote-editor"); }
    public String getName() { return "DNote Editor"; }
    public boolean getPreferSeparateWindow() { return false; }
    public boolean isSingleton() { return false; }
    public boolean showInMenus() { return true; }
  }
  /** TermInfoFactory inner class */
  private class TermInfoFactory implements GUIComponentFactory {
    public GUIComponent createComponent(String id) {
      TermInfo2 ti = new TermInfo2();
      ti.setMinimumSize(new Dimension(200,200));
      ti.setPreferredSize(new Dimension(200,200));
      return new GUIComponentWrapper(id, id, ti);
    }
    public FactoryCategory getCategory() { return FactoryCategory.ANNOTATION;  }
    public String getDefaultID() { return "term-info"; }
    /** These are alias IDs? */
    public List getIDs() { return CollectionUtil.list("term-info"); }
    public String getName() { return "Term Info"; }
    public boolean getPreferSeparateWindow() { return false; }
    public boolean isSingleton() { return false; }
    public boolean showInMenus() { return true; }
  }


  /** CharTableFactory inner class */
  private class CharTableFactory implements GUIComponentFactory {
    public GUIComponent createComponent(String id) {
      CharacterTableController tableController =
        new CharacterTableController(getDefaultGroup()); // for now just default
          JPanel ctp =  tableController.getCharacterTablePanel();
      // 1st is id, 2nd id -> title bar string
      return new GUIComponentWrapper(id, id, ctp);
    }
    public FactoryCategory getCategory() { return FactoryCategory.ANNOTATION;  }
    public String getDefaultID() { return "DNote-annotation-table-viewer"; }
    /** These are alias IDs? */
    public List getIDs() { return CollectionUtil.list("DNote-annotation-table-viewer"); }
    public String getName() { return "Annotation Table"; }
    public boolean getPreferSeparateWindow() { return false; }
    public boolean isSingleton() { return false; }
    public boolean showInMenus() { return true; }
  }


  private String getDefaultGroup() { return Config.inst().getDefaultGroup().getName(); }
}
