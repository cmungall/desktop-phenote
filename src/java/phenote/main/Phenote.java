package phenote.main;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import phenote.charactertemplate.CharacterTemplateController;
import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.config.xml.GroupDocument.Group;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.dataadapter.OntologyDataAdapter;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterIFactory;
import phenote.datamodel.CharacterListI;
import phenote.edit.EditManager;
import phenote.error.ErrorEvent;
import phenote.error.ErrorListener;
import phenote.error.ErrorManager;
import phenote.gui.CharacterTableController;
import phenote.gui.GridBagUtil;
import phenote.gui.LoadingScreen;
import phenote.gui.MenuManager;
import phenote.gui.SearchParams;
import phenote.gui.SelectionHistory;
import phenote.gui.ShrimpDag;
import phenote.gui.SplashScreen;
import phenote.gui.StandardToolbar;
import phenote.gui.TermInfo2;
import phenote.gui.field.FieldPanel;
import phenote.gui.selection.SelectionManager;
import phenote.servlet.PhenoteWebConfiguration;
import phenote.util.FileUtil;

public class Phenote {

  private static Logger LOG = Logger.getLogger(Phenote.class);
  private static boolean standalone = false; // default for servlet

  private FieldPanel mainFieldPanel;
  private static Phenote phenote;
//  private TermInfo termInfo;
  private TermInfo2 termInfo;
  private SelectionHistory selectionHistory;
  private CharacterTableController tableController;
  private CommandLine commandLine = CommandLine.inst();
  private JFrame frame;
  public StandardToolbar standardToolbar;
  public SplashScreen splashScreen;
  public LoadingScreen loadingScreen;
  private String logoFile = "images/phenote_logo.jpg";    



  //  public static Keymap defaultKeymap;
  

  public static void main(String[] args) {
    standalone = true; // i think this is ok
 
    // Check whether the root logger has an appender; there does not appear
    // to be a more direct way to check whether Log4J has already been 
    // initialized.  Note that the root logger's appender can always be
    // set to a NullAppender, so this does not restrict the utility of
    // the logging in any way.
    Logger rl = LogManager.getRootLogger();
    Enumeration<?> appenders = rl.getAllAppenders();
    if (!appenders.hasMoreElements()) {
      System.out.println("Log4J configuration failed, using default configuration settings");
      BasicConfigurator.configure(); 
      rl.setLevel(Level.DEBUG);
      LOG = LogManager.getLogger(Phenote.class);
    }
    org.apache.log4j.FileAppender a = (org.apache.log4j.FileAppender)rl.getAppender("MAIN");
    if (a != null) System.out.println("log file: "+a.getFile());
    else System.out.println("No MAIN File Appender for log4j");

    if (a.getFile() != null) {
      File f = new File(a.getFile()); //"phenote_log4j.log");
      System.out.println("path of file "+f.getPath()+" absolute "+f.getAbsolutePath()+" canWrite "+f.canWrite());
    }
    else {
      System.out.println("file for MAIN log appender is null");
    }

    // writes error events to log
    ErrorManager.inst().addErrorListener(new LogErrorListener());
    String v = "This is Phenote version "+PhenoteVersion.versionString();
    System.out.println(v);
    LOG.info(v);
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
      LOG.error("Look and feel class not found", e);
    } catch (InstantiationException e) {
      LOG.error("Could not instantiate look and feel", e);
    } catch (IllegalAccessException e) {
      LOG.error("Error setting look and feel", e);
    } catch (UnsupportedLookAndFeelException e) {
      LOG.error("Look and feel not supported", e);
    }

    phenote = getPhenote();
    boolean enableSplashScreen = !phenote.commandLine.writeIsSpecified();
    //System.out.println("splash "+enableSplashScreen);
    phenote.splashScreenInit(enableSplashScreen); //initialize the splash screen;
    phenote.doCommandLine(args); // does config

    //new phenote.gui.ConfigGui(); // testing out
    
 /*   set up the overall task of loading the software
    there will be several events:
    1.  loading configuration - display config name
    2.  checking for ontology updates - per ontology display name
    3.	updating ontologies (name) - downloading... display name
    4.	loading into datamodel - display name - this is the bulk of the time
  */    

    phenote.splashScreen.setProgress("Configuring...", 10);
    phenote.loadingScreen.setMessageText("Loading configuration: "+Config.inst().getConfigName());
    phenote.loadingScreen.setProgress(5);
    phenote.splashScreen.setProgress("Initializing Ontologies...", 20);
    phenote.loadingScreen.setMessageText("Initializing Ontologies");
    phenote.loadingScreen.setProgress(10);
//    phenote.loadingScreen.setProgress(20);
//    boolean done = false;
    
    phenote.initOntologies();
    phenote.splashScreen.setProgress("Ontologies Initialized", 70);
    phenote.loadingScreen.setMessageText("Ontologies Initialized");
//    phenote.loadingScreen.setProgress(70);
    phenote.loadFromCommandLine();  // aft ontols, reads from cmd line if specified
    phenote.splashScreen.setProgress("Phenote Loaded", 100);
    phenote.loadingScreen.setMessageText("Phenote Loaded");
    phenote.loadingScreen.setProgress(100);
    if (phenote.commandLine.writeIsSpecified()) {
      phenote.writeFromCommandLine();
      // it hangs after writing - not sure why
      System.out.println("Done writing, exiting phenote");
      System.exit(0);
    }
    else	// init gui if not writing (& reading) from cmd line
    {
    	phenote.initGui();
    	phenote.splashScreenDestruct();
//    	phenote.loadingScreenDestruct();
    }

    if (Config.inst().dataInputServletIsEnabled()) 
      new phenote.servlet.DataInputServer();

  }	

  /** private constructor -> singleton */
  private Phenote() {}


  public void initOntologies() {
    //OntologyDataAdapter oda = new OntologyDataAdapter(); // singleton?
    // loads up OntologyManager - non intuitive?
    OntologyDataAdapter.initialize();
    // if (config.useShrimpDagViewer())
    // ShrimpDag.inst().initOntologies();
  }

  private void loadFromCommandLine() {
    //LOG.debug("read spec "+commandLine.readIsSpecified());
    if (!commandLine.readIsSpecified()) return;
    // LOG.info("Reading "+blah+" from command line");
    try { commandLine.getReadAdapter().load(); }
    catch (Exception e) { LOG.error("Failed to do load via command line "+e); }
  }

  private void writeFromCommandLine() {
    if (!commandLine.writeIsSpecified()) return;
   try { commandLine.getWriteAdapter().commit(getCharList()); }
    catch (Exception e) { LOG.error("Failed to do write via command line "+e); }
  }

  private CharacterListI getCharList() {
    return CharacterListManager.inst().getCharacterList();
  }
  
  public void initGui() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        makeMainWindow();
        createGroupWindows();
      }
    });
  }

  private void createGroupWindows() {
    for (Group group : getGroupsByContainer(Group.Container.WINDOW)) {
      if (group.getInterface().equals(Group.Interface.CHARACTER_TEMPLATE)) {
        createTemplateController(group.getGroupAdapter(), group.getName());
      }
    }
  }
  
  private CharacterTemplateController createTemplateController(String className, String groupName) {
    final String errorMessage = "Failed creating CharacterTemplate";
    try {
      Class<?> adapterClass = Class.forName(className);
      Constructor<?> constructor = adapterClass.getConstructor(String.class);
      Object templateController = constructor.newInstance(groupName);
      return (CharacterTemplateController)templateController;
    } catch (ClassNotFoundException e) {
      LOG.error(errorMessage, e);
    } catch (InstantiationException e) {
      LOG.error(errorMessage, e);
    } catch (IllegalAccessException e) {
      LOG.error(errorMessage, e);
    } catch (NoSuchMethodException e) {
      LOG.error(errorMessage, e);
    } catch (InvocationTargetException e) {
      LOG.error(errorMessage, e);
    }
    return null;
  }

  /** do group guis specified for a tab(not window) - for tab in main window */
  private void createGroupTabs() {
    for (Group group : getGroupsByContainer(Group.Container.TAB)) {
      
    }

  }

  private boolean hasGroupTabs() {
    return !getGroupTabs().isEmpty();
  }

  private int getNumGroupTabs() {
    if (getGroupTabs() == null) return 0;
    return getGroupTabs().size();
  }

  private List<Group> getGroupTabs() {
    return getGroupsByContainer(Group.Container.TAB);
  }

  /** Get groups specifically designated for a container - window or tab */
  private List<Group> getGroupsByContainer(Group.Container.Enum container) {
    List<Group> l = new ArrayList<Group>();
    for (Group group : Config.inst().getFieldGroups()) {
      if (group.getContainer() == container) // only do tabs
        l.add(group);
    }
    return l;
  }


  private void splashScreenInit(boolean enable) {
  	ImageIcon myImage = new ImageIcon();
  	try {
      myImage = new ImageIcon(FileUtil.findUrl(logoFile));
  	}	catch (FileNotFoundException ex) {  }
 	
//    ImageIcon myImage = new ImageIcon(logoFile);
    splashScreen = new SplashScreen(myImage,enable);
    loadingScreen = new LoadingScreen();
    
    if (!enable) return;
//    splashScreen.setLocationRelativeTo(null);
//    splashScreen.setProgressMax(100);
//    splashScreen.setScreenVisible(true);
    loadingScreen.setScreenVisible(true);
    loadingScreen.setLocationRelativeTo(null); // centers panel on screen
//    splashScreen.setProgress("Phenote version "+PhenoteVersion.versionString(), 0);
  }

  private void splashScreenDestruct() {
    splashScreen.setScreenVisible(false);
    loadingScreen.setScreenVisible(false);
  }
  
  private void doCommandLine(String[] args) {
    try { commandLine.setArgs(args); } // sets config if specified
    catch (Exception e) { // no log yet - sys.out
      System.out.println("Command line read failed: "+e); }//e.printStackTrace();
    // no config set from command line use default
    if (!Config.inst().isInitialized()) { 
      try { Config.inst().loadDefaultConfigFile(); }
      catch (ConfigException ce) { 
        System.out.println("default config has failed. "+ce+" loading flybase default");
        try { Config.inst().loadDefaultFlybaseConfigFile(); }
        catch (ConfigException c) { 
          System.out.println("flybase default config has failed. We're hosed! "+c);
        }
      }
    }
  }

//  private void ontologyProgressChangeListener extends PropertyChangeListener {
//  	public void propertyChange(PropertyChangeEvent evt) {
//  		if (!done) {
//  			int progress = task.getProgress();
//  			String m = "some ontology";
//  			loadingScreen.setProgress(m, progress);
//    }



  public Frame getFrame() { return frame; }

  private void makeMainWindow() {
    // this may be changed to applet...
    frame = new JFrame("Phenote "+PhenoteVersion.versionString()); 
    if (getNumGroupTabs() < 2) {
      Group g = Config.inst().getDefaultGroup();
      frame.getContentPane().add(makeGroupPanel(g));
    }
    else {
      JTabbedPane tabbedGroups = new JTabbedPane();
      for (Group g : getGroupTabs()) {
        JPanel p = makeGroupPanel(g); //.getName());
        tabbedGroups.add(g.getTitle(),p);
      }
      frame.getContentPane().add(tabbedGroups);
    }
    MenuManager.createMenuManager(frame);
    frame.setPreferredSize(new Dimension(1220,800)); //1100,700));
    if (standalone) // if stand alone exit java on window close
      frame.addWindowListener(new WindowExit());
    standardToolbar = new StandardToolbar();
    frame.add(standardToolbar, BorderLayout.NORTH);

    frame.pack();
    frame.setVisible(true);
  }

  /** for standalone & webstart - not for servlet as i think it will bring the
      whole servlet down */
  private class WindowExit extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
    	String m="";
    	int n;
      if (standalone) {
    		if (Config.inst().isConfigModified()) {
    			m="Your Default Settings have been modified.  Do you wish to save them?";
    			n = JOptionPane.showConfirmDialog(null,m,"Configuration Alert", JOptionPane.YES_NO_OPTION);
    			if (n==JOptionPane.YES_OPTION) { 
    				Config.inst().setAutocompleteSettings();
    				//write out configuration!
    		    Config.inst().saveModifiedConfig();
    		    m="";
    			}
    			else { 
    				m="Your changes have not been saved.\n";
    			}
    		}
//    		else {
//    			System.out.println("your settings have not changed");
//    		}
//    	  m += "Are you sure you want to quit?";
//    	  n = JOptionPane.showConfirmDialog(null, m, "Phenote Exit",
//    			  JOptionPane.YES_NO_OPTION);
//    	  if (n==JOptionPane.YES_OPTION) {
//    	  	//  now quit
//    	  	System.exit(0);
//    	  } else { return; }
    		System.exit(0);
     	} 
    }
  }

  /** main panel contains FieldPanel CharTablePanel & TermInfo 
      a group panel now holds all 3 of these things
      move this to gui? */
  private JPanel makeGroupPanel(Group group) {
    JPanel mainPanel = new JPanel(new GridLayout());
    
    JPanel infoHistoryPanel = new JPanel(new GridBagLayout());
    infoHistoryPanel.setBorder(new EmptyBorder(10,10,10,10));
    
    this.tableController = new CharacterTableController(group.getName());
    
    // need to do different selection & edit mgrs
    FieldPanel groupFieldPanel = new FieldPanel(true,false,group.getName(), this.tableController.getSelectionModel());
    groupFieldPanel.setBorder(new EmptyBorder(10,10,10,10));
    // for testing - thats it
    if (group == null || group.getName().equals("default"))
      mainFieldPanel = groupFieldPanel;

//    termInfo = new TermInfo();
    termInfo = new TermInfo2();
    GridBagConstraints ugbc = GridBagUtil.makeFillingConstraint(0,0);
    ugbc.weightx = 5;
    infoHistoryPanel.add(termInfo.getComponent(),ugbc);
    
    selectionHistory = SelectionHistory.inst();
    ugbc.gridx++;
    ugbc.weightx = 3;
    infoHistoryPanel.add(selectionHistory.getComponent(),ugbc); 
    
    JSplitPane innerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, groupFieldPanel, infoHistoryPanel);
    innerSplitPane.setDividerLocation(700);
    
    
    JPanel ctp =  this.tableController.getCharacterTablePanel();
    JSplitPane outerSplitPane =
      new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, innerSplitPane,ctp);
    mainPanel.add(outerSplitPane);
    return mainPanel;
  }
  
  /** this doesnt work yet - OntologyManager list of char fields is not getting 
      reset for one thing - slippery slope this stuff */
  public void changeConfig(String newCfg) { // throws ConfigException??

    try {
      // load new config
      // Config loadDefaultConfigFile?? as its set above w writeMyPhen?
      Config.changeConfig(newCfg); // for now just does update with new version
      
      // wipe out old data
      CharacterListManager.inst().clear();
      
      // load ontologies - reuse ones that are already loaded? dump ones not used?
      // this needs to reset onotologymanagers charfields - or something does??
      // ontolMan.clear()?
      initOntologies(); // ?? i think will preserve ontol cache which is good
      
      // wipe out & bring up new gui
      phenote.initGui(); // or phenote.reinitGui()?

    }
    catch (ConfigException e) { // throw it??
      String m = "Failed to change configuration " + e.getMessage();
      JOptionPane.showMessageDialog(null, m, "Config error",
                                    JOptionPane.ERROR_MESSAGE);
    }

  }



  public static Phenote getPhenote() {  // singleton
    if (phenote == null) phenote = new Phenote();
    return phenote;
  }
  
  public static void reset() {
    phenote = null;
  }
  
  
  /**
   * This is used to reset state between unit tests.
   */
  public static void resetAllSingletons() {
    CharacterListManager.reset();
    LoadSaveManager.reset();
    OntologyDataAdapter.reset();
    CharacterIFactory.reset();
    CharFieldManager.reset();
    EditManager.reset();
    ErrorManager.reset();
    MenuManager.reset();
    SearchParams.reset();
    SelectionHistory.reset();
    ShrimpDag.reset();
    SelectionManager.reset();
    CommandLine.reset();
    PhenoteWebConfiguration.reset();
    Config.reset();
    Phenote.reset();
  }

  /** listens for errors and shoots them to the log */
  private static class LogErrorListener implements ErrorListener {
    public void handleError(ErrorEvent e) {
      Logger log = Logger.getLogger(e.getSource().getClass());
      log.error(e.getMsg());
    }
  }

  // These methods are actually for TestPhenote
  public FieldPanel getFieldPanel() { return mainFieldPanel; }
//  public TermInfo getTermInfo() { return termInfo; }
  //public CharacterTablePanel getCharacterTablePanel() { return characterTablePanel; }
  // for TestPhenote - silly method as one created for each group
  public CharacterTableController getCharacterTableController() { return this.tableController; }

  public TermInfo2 getTermInfo() {
  	return termInfo;
  }

}

//   /** args is most likely null if not called from command line */
//   public void initConfig(String[] args) {
//     // gets config file from command line & loads - if no config file 
//     // loads default. should actually put that logic here.
//     doCommandLine(args); // load config file
//   }

