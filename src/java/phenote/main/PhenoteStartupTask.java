package phenote.main;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import main.ProtocolEditor;

import org.apache.log4j.Logger;
import org.bbop.framework.AbstractComponentFactory;
import org.bbop.framework.GUIComponent;
import org.bbop.framework.GUIComponentFactory;
import org.bbop.framework.GUIComponentWrapper;
import org.bbop.framework.GUIManager;
import org.bbop.framework.GUITask;
import org.bbop.framework.MainFrame;
import org.bbop.framework.ScreenLockTask;
import org.bbop.framework.ViewMenu;
import org.bbop.util.CollectionUtil;
import org.oboedit.example.OBDAnnotationNumberFetchBehaviorTask;
import org.oboedit.gui.Preferences;
import org.oboedit.gui.factory.AnnotationSummaryComponentFactory;
import org.oboedit.gui.factory.DAGViewFactory;
import org.oboedit.gui.factory.GraphDAGViewFactory;
import org.oboedit.gui.factory.GraphEditorFactory;
import org.oboedit.gui.tasks.DefaultGUIStartupTask;

import phenote.charactertemplate.CharacterTemplateTableFactory;
import phenote.charactertemplate.TemplateChooserFactory;
import phenote.config.Config;
import phenote.config.xml.GroupDocument.Group;
import phenote.config.xml.TemplatechooserDocument.Templatechooser;
import phenote.gui.NcbiInfo;
import phenote.gui.PhenoteMainFrame;
import phenote.gui.StandardToolbar;
import phenote.gui.TermInfo2;
import phenote.gui.actions.AboutAction;
import phenote.gui.factories.CharacterTableFactory;
import phenote.gui.factories.NCBIInfoFactory;
import phenote.gui.factories.PhenoteDAGViewFactory;
import phenote.gui.factories.PhenoteEditorFactory;
import phenote.gui.factories.PhenoteGraphDAGViewFactory;
import phenote.gui.factories.ProtocolEditorFactory;
import phenote.gui.factories.TermInfoFactory;
import phenote.gui.field.FieldPanelContainer;
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
//		factories.add(new FieldPanelFactory());
		factories.add(new PhenoteEditorFactory());
		factories.add(new TermInfoFactory());
		factories.add(new NCBIInfoFactory());
		factories.addAll(this.getCharacterTableComponentFactories());
//		factories.add(new GraphEditorFactory());  <-- removing this for upcoming 1.5 release
		factories.add(new PhenoteDAGViewFactory());
		factories.add(new PhenoteGraphDAGViewFactory());
		factories.add(new AnnotationSummaryComponentFactory());
		factories.addAll(this.getTemplateGroupComponentFactories());
    factories.add(new ProtocolEditorFactory());
		return factories;
	}

	/** this is called at initialization i believe */
	@Override
	protected void doOtherInstallations() {
		LOG.debug("doOtherInstallations called");
		
		// new Throwable().printStackTrace();
		//i'm removing this here because i think its being called twice!  yep
		//		initPhenote();
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
		return "Phenote+"; // "DScribe"???
	}

	protected Action getAboutAction() {
		return new AboutAction();
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
			return getPerspectiveResourceDir() + "/phenote_classic.idw";
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
				new OBDAnnotationNumberFetchBehaviorTask()); // TODO: for
		// testing
	}
	
	@Override
	protected JFrame createFrame() {
		JFrame out = new PhenoteMainFrame(getAppID());
		out.setTitle(getAppName());
		return out;
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

	//The following were moved into external factory classes in phenote.gui.factories
//	/** FieldPanelFactory inner class */
//	private class FieldPanelFactory extends AbstractComponentFactory<FieldPanelContainer> {
//
//		private String panelName = "Phenote Editor";
//
//		public FactoryCategory getCategory() {
//			return FactoryCategory.ANNOTATION;
//		}
//
//		public String getName() {
//			return panelName;
//		}
//
//		public String getID() {
//			return "phenote-editor";
//		}
//		
//		public boolean isSingleton() {
//      return true;
//    }
//
//		@Override
//		public FieldPanelContainer doCreateComponent(String id) {
//			return new FieldPanelContainer(id);
//		}
//	}
//
//	/** TermInfoFactory inner class */
//	private class TermInfoFactory extends AbstractComponentFactory<TermInfo2> {
//		public FactoryCategory getCategory() {
//			return FactoryCategory.ANNOTATION;
//		}
//
//		public String getName() {
//			return "Term Info";
//		}
//
//		public boolean isSingleton() {
//			return true;
//		}
//
//		public String getID() {
//			return "term-info";
//		}
//
//		@Override
//		public TermInfo2 doCreateComponent(String id) {
//			return TermInfo2.inst();
//		}
//	}

//	/** NCBIInfoFactory inner class */
//	private class NCBIInfoFactory extends AbstractComponentFactory<NcbiInfo> {
//		private String panelName = "NCBI";
//		private String displayName = null;
//
//		public FactoryCategory getCategory() {
//			return FactoryCategory.INFO;
//		}
//
//		public String getName() { // what is displayed in the menu
//			return panelName;
//		}
//
//		public String getID() {
//			return "NCBI";
//		}
//
//		@Override
//		public NcbiInfo doCreateComponent(String id) {
//			NcbiInfo info = new NcbiInfo();
//			info.setMinimumSize(new Dimension(200, 200));
//			info.setPreferredSize(new Dimension(200, 200));
//			info.setTitle("NCBI");
//			return info;
//		}
//
//	}

//	/** StandardToolbarFactory inner class */
//	private class StandardToolbarFactory extends AbstractComponentFactory<StandardToolbar> {
//
//		public FactoryCategory getCategory() {
//			return FactoryCategory.TOOLBARS;
//		}
//
//		public String getName() {
//			return "Standard Toolbar";
//		}
//
//		public String getID() {
//			return "Standard toolbar";
//		}
//
//  	@Override
//	public StandardToolbar doCreateComponent(String id) {
//  		return new StandardToolbar();
//  	}
////		@Override
////		public GUIComponentWrapper doCreateComponent(String id) {
////			return new GUIComponentWrapper(id, id, new StandardToolbar());
////		}
//	}

//  private class ProtocolEditorFactory extends AbstractComponentFactory<GUIComponentWrapper> {
//
//    public FactoryCategory getCategory() {
//      return FactoryCategory.ANNOTATION;
//    }
//    public String getName() { return "Protocol Editor"; }
//    public String getID() { return "protocol-editor"; }
//    public GUIComponentWrapper doCreateComponent(String id) {
//      JPanel p = ProtocolEditor.getUniqueInstance().getMainPanel();
//      // 1st is id, 2nd id -> title bar string
//      return new GUIComponentWrapper(id, id, p);
//    }
//  }

	private String getDefaultGroup() {
		return Config.inst().getDefaultGroup().getName();
	}
	
	private Logger log() {
    return Logger.getLogger(this.getClass());
  }
}
