package phenote.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JMenuItem;

import org.apache.log4j.Logger;

import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIComponentWrapper;
import org.bbop.framework.ViewMenu;
import org.bbop.util.CollectionUtil;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;

import phenote.gui.field.FieldPanel;

public class PhenoteStartupTask extends DefaultGUIStartupTask {

  Logger LOG =  Logger.getLogger(PhenoteStartupTask.class);

	@Override
	protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
		//Collection<GUIComponentFactory<?>> factories = super.getDefaultComponentFactories();
    Collection<GUIComponentFactory<?>> factories =
      new ArrayList<GUIComponentFactory<?>>();
		factories.add(new FieldPanelFactory());
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
    // cmd line??? dont think john/oboedit can use phenotes open src townsend posix
    // standard stuff so would be good if could have access to cmd line args
    // oh duh - do from Phenote2!!!


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


  /** PhenoteCompFactory inner class */
  private class FieldPanelFactory implements GUIComponentFactory {
    public GUIComponent createComponent(String id) {
      //String l = "<html><h1>This is an example component</h1></html>";
      // 1st is id, 2nd id -> title bar string
      //return new GUIComponentWrapper(id, id, new JLabel(l));
      // for now just doing default group - need to iterate groups
      FieldPanel groupFieldPanel = new FieldPanel(); //true,false,Config.inst().getDefaultGroup(), this.tableController.getSelectionModel());
      return new GUIComponentWrapper(id, id, groupFieldPanel);
    }

    public FactoryCategory getCategory() {
      return FactoryCategory.ANNOTATION;
    }

    public String getDefaultID() {
      return "dnote-editor";
    }

    /** This is not the ids above used in createComponent??
     */
    public List getIDs() {
      return CollectionUtil.list("dnote-editor");
    }

    public String getName() {
      return "DNote Editor";
    }

    public boolean getPreferSeparateWindow() {
      return false;
    }

    public boolean isSingleton() {
      return false;
    }

    public boolean showInMenus() {
      return true;
    }


  }

}
