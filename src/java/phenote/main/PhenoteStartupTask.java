package phenote.main;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import org.bbop.framework.AbstractGUIComponent;
import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIComponentWrapper;
import org.bbop.framework.GUIManager;
import org.bbop.framework.GUITask;
import org.bbop.framework.ViewMenu;
import org.bbop.util.CollectionUtil;
//import org.oboedit.example.AnnotationNumberFetchBehaviorTask;
import org.oboedit.example.OBDAnnotationNumberFetchBehaviorTask;
import org.oboedit.gui.Preferences;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;
import org.bbop.framework.ScreenLockTask;
import org.oboedit.gui.components.AnnotationSummaryComponent;
import org.oboedit.gui.factory.AnnotationSummaryComponentFactory;
import org.oboedit.gui.factory.DAGViewFactory;
import org.oboedit.gui.factory.GraphDAGViewFactory;
import org.oboedit.gui.factory.GraphEditorFactory;

import phenote.config.Config;
import phenote.gui.CharacterTableController;
import phenote.gui.TermInfo2;
import phenote.gui.NcbiInfo;
import phenote.gui.field.FieldPanel;
import phenote.gui.HelpMenu;
import phenote.gui.selection.SelectionBridge;
import phenote.gui.menu.FileMenu;
import phenote.gui.menu.EditMenu;
import phenote.gui.menu.PhenoteHelpMenu;
import phenote.gui.menu.SettingsMenu;
import phenote.gui.StandardToolbar;


public class PhenoteStartupTask extends DefaultGUIStartupTask {

	private Logger LOG = Logger.getLogger(PhenoteStartupTask.class);
	private String[] args;
	private SelectionBridge selectionBridge = new SelectionBridge();

	PhenoteStartupTask(String[] args) {
		this.args = args;
	}

	@Override
	protected Collection<GUIComponentFactory<?>> getDefaultComponentFactories() {
		// Collection<GUIComponentFactory<?>> factories =
		// super.getDefaultComponentFactories();
		Collection<GUIComponentFactory<?>> factories = new ArrayList<GUIComponentFactory<?>>();
		factories.add(new FieldPanelFactory());
		factories.add(new TermInfoFactory());
		factories.add(new NCBIInfoFactory());
		factories.add(new CharTableFactory());
		factories.add(new GraphEditorFactory());
		factories.add(new DAGViewFactory());
		factories.add(new GraphDAGViewFactory());
		factories.add(new StandardToolbarFactory());
		factories.add(new AnnotationSummaryComponentFactory());
		return factories;
	}

	/** this is called at initialization i believe */
	@Override
	protected void doOtherInstallations() {
		LOG.debug("doOtherInstallations called");
		// new Throwable().printStackTrace();
		initPhenote();
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
		return "Phenote"; // "DScribe"???
	}

	protected Action getAboutAction() {
		return new AbstractAction("About") {

			public void actionPerformed(ActionEvent actionEvent) {
				(new HelpMenu()).showAboutFrame();
			}
		};
	}

	@Override
	// ??
	protected String getPerspectiveResourceDir() { // not using org
		return "phenote/gui/layout/resources"; // need to add this
		// "org/phenote/gui/layout/resources";
		// return System.getProperty("user.home") + "/.phenote";
	}
	
	@Override
	protected String getDefaultPerspectiveResourcePath() {
		if (getPerspectiveResourceDir() != null)
			return getPerspectiveResourceDir() + "/edit.idw";
		else
			return null;
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
				new OBDAnnotationNumberFetchBehaviorTask()); // TODO: for testing
	}

	/** FieldPanelFactory inner class */
	private class FieldPanelFactory implements GUIComponentFactory {
		
		// private String panelName = "DScribe Editor";
		private String panelName = "Phenote Editor";
		private String displayName = null;
		
		public GUIComponent createComponent(String id) {
			String l = "<html><h1>This is an example component</h1></html>";
			// if (true) return new GUIComponentWrapper(id, id, new
			// javax.swing.JLabel(l));
			// for now just doing default group - need to iterate groups
			FieldPanel groupFieldPanel = new FieldPanel(); // true,false,Config.inst().getDefaultGroup(),
															// this.tableController.getSelectionModel());
			groupFieldPanel.setMinimumSize(new Dimension(300, 300));
			groupFieldPanel.setPreferredSize(new Dimension(300, 300));
			// 1st is id, 2nd id -> title bar string
			String configName = groupFieldPanel.getName();
			groupFieldPanel.setTitle(panelName+" (Configuration: "+configName+")");
			return groupFieldPanel;

			
//			return new GUIComponentWrapper(id, id, groupFieldPanel);
		}

		public FactoryCategory getCategory() {
			return FactoryCategory.ANNOTATION;
		}
		
		public String getName() {
			return panelName;
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

		public String getID() {
			return "phenote-editor";
		}

    public boolean isRestoreOnStartup() {
      // TODO Auto-generated method stub
      return true;
    }
	}

	/** TermInfoFactory inner class */
	private class TermInfoFactory implements GUIComponentFactory {
		private String panelName = "Term Info";
		private String displayName = "Term Info";

		public GUIComponent createComponent(String id) {
			return TermInfo2.inst();
			//			return new GUIComponentWrapper(id,id, ti.getComponent());
		}

		public FactoryCategory getCategory() {
			return FactoryCategory.ANNOTATION;
		}

		public String getName() {
			return "Term Info";
		}

		public boolean getPreferSeparateWindow() {
			return false;
		}

		public boolean isSingleton() {
			return true;
		}

		public boolean showInMenus() {
			return true;
		}

		public String getID() {
			return "term-info";
		}

    public boolean isRestoreOnStartup() {
      // TODO Auto-generated method stub
      return true;
    }
		
//		public void setName(String name) {
//			displayName = panelName+": "+name;
//		}
	}
	
	/** NCBIInfoFactory inner class */
	private class NCBIInfoFactory implements GUIComponentFactory {
		private String panelName = "NCBI";
		private String displayName = null;

		public GUIComponent createComponent(String id) {
			NcbiInfo info = new NcbiInfo();
			info.setMinimumSize(new Dimension(200, 200));
			info.setPreferredSize(new Dimension(200, 200));
			info.setTitle("NCBI");
			return info;
			
			//			this.setName("(none)");
//			return new GUIComponentWrapper(id, id, info.getComponent());
		}

		public FactoryCategory getCategory() {
			return FactoryCategory.INFO;
		}

		public String getName() { //what is displayed in the menu
			return panelName;
		}

		public boolean getPreferSeparateWindow() {
			return false;
		}

		public boolean isSingleton() {
			return true;
		}

		public boolean showInMenus() {
			return true;
		}

		public String getID() {
			return "NCBI";
		}

    public boolean isRestoreOnStartup() {
      // TODO Auto-generated method stub
      return true;
    }

	}


	/** CharTableFactory inner class */
	private class CharTableFactory implements GUIComponentFactory {
		private String panelName = "Annotation Table";
		private String displayName = null;

		public GUIComponent createComponent(String id) {
			CharacterTableController tableController = new CharacterTableController(
					getDefaultGroup()); // for now just default
			JPanel ctp = tableController.getCharacterTablePanel();
			// 1st is id, 2nd id -> title bar string
//			this.setName("(new)");
			return new GUIComponentWrapper(id, id, ctp);
//			return (AbstractGUIComponent)ctp;
		}

		public FactoryCategory getCategory() {
			return FactoryCategory.ANNOTATION;
		}

		public String getName() {
			return panelName;
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
		public String getID() {
			return "Annotation Table";
		}

    public boolean isRestoreOnStartup() {
      // TODO Auto-generated method stub
      return true;
    }
	}

	/** StandardToolbarFactory inner class */
	private class StandardToolbarFactory implements GUIComponentFactory {
		public GUIComponent createComponent(String id) {
			StandardToolbar toolbar = new StandardToolbar();
			// 1st is id, 2nd id -> title bar string
			return new GUIComponentWrapper(id, id, toolbar);
		}

		public FactoryCategory getCategory() {
                  // donest compile
                  //			return FactoryCategory.TOOLBARS;
                  return null;
		}

		public String getName() {
			return "Standard Toolbar";
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

		public String getID() {
			return "Standard toolbar";
		}

    public boolean isRestoreOnStartup() {
      // TODO Auto-generated method stub
      return true;
    }
	}

	private String getDefaultGroup() {
		return Config.inst().getDefaultGroup().getName();
	}
}
