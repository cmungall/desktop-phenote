package phenote.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JMenuItem;

import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIComponentWrapper;
import org.bbop.framework.ViewMenu;
import org.bbop.util.CollectionUtil;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;

public class PhenoteStartupTask extends DefaultGUIStartupTask {

	@Override
	protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
		Collection<GUIComponentFactory<?>> factories = super.getDefaultComponentFactories();
		factories.add(new PhenoteCompFactory());
		return factories;
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
		//return "phenote/gui/layout/resource";
    //"org/phenote/gui/layout/resources";
    return System.getProperty("user.home") + "/.phenote";
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
  private class PhenoteCompFactory implements GUIComponentFactory {
    public GUIComponent createComponent(String id) {
      String l = "<html><h1>This is an example component</h1></html>";
      return new GUIComponentWrapper(id, id, new JLabel(l));
    }

    public FactoryCategory getCategory() {
      return FactoryCategory.MISC;
    }

    public String getDefaultID() {
      return "main";
    }

    public List getIDs() {
      return CollectionUtil.list("example_component");
    }

    public String getName() {
      return "Example Component";
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
