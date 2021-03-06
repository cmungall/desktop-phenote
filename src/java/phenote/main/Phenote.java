package phenote.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.config.Preferences;
import phenote.config.xml.GroupDocument.Group;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.dataadapter.OntologyDataAdapter;
import phenote.dataadapter.OntologyDataAdapter2;
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
  private final static int WINDOW_WIDTH = 1220; // 1100
  private final static int WINDOW_HEIGHT = 800;  // 700
  //private final static boolean USE_LOADING_SCREEN = true; -> Config

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
  private static String logoFile = "images/phenote_logo.png";
  private static JDialog bePatientDialog = null;        

  public static void main(String[] args) {
    standalone = true; // i think this is ok
 
    init(args);
  }	

  /** private constructor -> singleton */
  private Phenote() {}

  private static void init(String[] args) {
    initBackend(args,true);
    LOG.debug("Backend initialized, initializing GUI");
    phenote.initGui();
    phenote.splashScreenDestruct(); // initBackend startsup splash
    LOG.info("Done with init--ready to go!");
  }

  /** args are command line arguments
      if doSplash is true then display phenote splash screen.
      the splash screen comes up blank for plus - needs to be fixed as
      its a nice splash. even worse on some linuxes the splash is blank
      and hangs in front of phenote - so doSplash false allows for
      disabling the splash til these bugs are fixed */
  static void initBackend(String[] args,boolean doSplash) {
    initLogger();
    initLookAndFeel();
    phenote = getPhenote();
    //phenote.splashScreenInit();
    // command line & config should be separated? - refactor?
    phenote.doCommandLineAndConfig(args); // does config
    // have to init splash screen after config as config dictates whether to
    // show splash screen (loading screen can threadlock)
//    LOG.debug("initBackend: doSplash = " + doSplash);  // DEL
//    if (doSplash)
//	    phenote.splashScreenInit();
//    else
//	    showBePatientDialog();

    phenote.initOntologies();
    // phenote sometimes hangs right around here!???!
//    LOG.debug("Ontologies initialized, checking command line for read & write");
    phenote.loadFromCommandLine(); // if load/file specified from cmd line
    phenote.setProgress("Phenote loaded", 100);
    phenote.writeFromCommandLine(); // if cmd line, writes & exits
    phenote.initDataInputServlet();
  }

  private static void initLogger() {
    // Check whether the root logger has an appender; there does not appear
    // to be a more direct way to check whether Log4J has already been 
    // initialized.  Note that the root logger's appender can always be
    // set to a NullAppender, so this does not restrict the utility of
    // the logging in any way.
    Logger rl = LogManager.getRootLogger();
    Enumeration<?> appenders = rl.getAllAppenders();
    if (appenders==null || !appenders.hasMoreElements()) {
      System.out.println("Log4J configuration failed, using default configuration settings");
      BasicConfigurator.configure(); 
      rl.setLevel(Level.DEBUG);
      LOG = LogManager.getLogger(Phenote.class);
    }

    // hmm dont knwo about this code, tries to write out filepath of log file to
    // stdout, but assumes its configured with MAIN which may not be the case.
    // maybe should just get whatever appender it can get rather than just MAIN
    // maybe do getAllAppenders and do this for each one? in any case taking out
    // misleading error messages
    org.apache.log4j.FileAppender a = (org.apache.log4j.FileAppender)rl.getAppender("MAIN");
    if (a != null) System.out.println("log file: "+a.getFile());
    //else System.out.println("No MAIN File Appender for log4j");

    if (a != null && a.getFile() != null) {
      File f = new File(a.getFile()); //"phenote_log4j.log");
      System.out.println("path of file "+f.getPath()+" absolute "+f.getAbsolutePath()
                         +" canWrite "+f.canWrite());
    }
    //else { System.out.println("file for MAIN log appender is null"); }

    // writes error events to log - this should probably be refactored to just use
    // log4j straight up, and have log4j appender for an error window
    // error maanager used to go to term info but no longer
    ErrorManager.inst().addErrorListener(new LogErrorListener());
    logInfo("This is Phenote version "+PhenoteVersion.versionString());
  }

  private static void initLookAndFeel() {
//    try {
//      final String lookAndFeelClassName = UIManager.getSystemLookAndFeelClassName();
//      final String osName = System.getProperty("os.name");
//      if (osName.startsWith("Mac")) {
//        // We are running on Mac OS X - use the Quaqua look and feel
//        System.setProperty("apple.laf.useScreenMenuBar", "true");
//        UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
//      } else {
//        // We are on some other platform, use the system look and feel
//        UIManager.setLookAndFeel(lookAndFeelClassName);
//      }
//    } catch (ClassNotFoundException e) {
//      LOG.error("Look and feel class not found", e);
//    } catch (InstantiationException e) {
//      LOG.error("Could not instantiate look and feel", e);
//    } catch (IllegalAccessException e) {
//      LOG.error("Error setting look and feel", e);
//    } catch (UnsupportedLookAndFeelException e) {
//      LOG.error("Look and feel not supported", e);
//    }
  }

  public static void showBePatientDialog() {
//          LOG.debug("showBePatientDialog"); // DEL
    bePatientDialog = new JDialog((Frame)null, "Reading ontology files--please wait", false);
    bePatientDialog.setPreferredSize(new Dimension(540, 250));
    bePatientDialog.setBackground(Color.WHITE);
    bePatientDialog.setLayout(new BorderLayout(10,10));
    bePatientDialog.setLocationRelativeTo(null); // centers panel on screen
    bePatientDialog.setEnabled(true);

    URL logoURL = null;
    try {
      logoURL = FileUtil.findUrl(logoFile);
    }
    catch (FileNotFoundException e) {
      LOG.error("Unable to find Phenote logo (" + logoFile + ")");
    }

    String imageString = (logoURL == null) ? "" : "<center><img vspace=15 src=\"" + logoURL + "\">";
    String myConfig = "";
    try {
      myConfig = " (" + Config.inst().getMyPhenoteConfigString() + ")";
    }
    catch (IOException e) {
    }

    bePatientDialog.add(new JLabel("<html><body bgcolor=\"#FFFFFF\">" + imageString +
                                   "<br><br><font size=4>Phenote is updating and loading the ontology files required by your configuration" + 
                                   myConfig +
                                   ".<br>" +
                                   "This process will take a while (possibly several minutes).</font><br><br>" +
                                   "<hr><br><font size=2>Phenote is a project of the Berkeley Bioinformatics Open-Source Projects" +
                                   " (<a href='http://www.berkeleybop.org'>BBOP</a>).<br>It is distributed under the FreeBSD license.</font></center><br></body></html>"),
                        BorderLayout.NORTH);

    bePatientDialog.pack();
    bePatientDialog.setVisible(true);
    bePatientDialog.toFront();
    LOG.debug("Displaying 'be patient' dialog..."); // DEL
  }

  public void initOntologies() {
  	//set up new interface here.
	
    String m = "initOntologies: loading configuration: "+Config.inst().getConfigName();
    logInfo(m);
    
    if (Config.inst().getTerminologyDefs()!=null) { //only do this if defined
            try {
                    OntologyDataAdapter2.getInstance().initOntologies();
            } catch (Exception e) {
                    e.printStackTrace();
            }
    } else { //the old-school config style
            setProgress(m, 10);
            setProgress(5); // 5?? from 10??? nicole?
            setProgress("Initializing Ontologies...", 20);
            setProgressMsg("Initializing Ontologies");
            setProgress(10);
            LOG.debug("Initializing ontologies");
            //OntologyDataAdapter oda = new OntologyDataAdapter(); // singleton?
            // loads up OntologyManager - non intuitive?
            OntologyDataAdapter.initialize(); // this sometimes hangs!!!
            setProgress("Ontologies initialized", 70);
            //setMessageText("Ontologies Initialized");
    }
  }

  // If user specified a file to load on the command line, read it now
  private void loadFromCommandLine() {
//    LOG.debug("read spec "+commandLine.readIsSpecified());
    if (!commandLine.readIsSpecified()) return;
    File input = new File(commandLine.getCommandLineFilenameToRead());
    if (input == null || !input.exists()) {
      LOG.error("Can't find requested file " + commandLine.getCommandLineFilenameToRead());
      return;
    }
    LOG.info("Trying to load command-line-specified file " + input);
//    try { commandLine.getReadAdapter().load(input); }
    try { LoadSaveManager.inst().loadData(input, commandLine.getReadAdapter()); }
    catch (Exception e) { LOG.error("Problem trying to load file " + input + " specified on command line: "+e.getMessage() + "; stack trace: ");  e.printStackTrace(); }
  }

  private void writeFromCommandLine() {
    if (!commandLine.writeIsSpecified()) return;
    try {
      commandLine.getWriteAdapter().commit(getCharList());
      logInfo("Done writing, exiting phenote");
    }
    catch (Exception e) { LOG.error("Failed to do write via command line "+e); }
    System.exit(0);
  }

  private void initDataInputServlet() {
    if (Config.inst().dataInputServletIsEnabled()) 
      new phenote.servlet.DataInputServer();
  }

  private CharacterListI getCharList() {
    return CharacterListManager.inst().getCharacterList();
  }
  
  public void initGui() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        LOG.debug("swing thread for gui started, making main window "+new java.util.Date());
//         try { Thread.sleep(20000); } catch(Exception e){LOG.debug("interrupt");}
//         LOG.debug("done sleeping - making main window "+new java.util.Date());
        makeMainWindow(); // with tabbed groups
      }
    });
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

  /** Note that this is not the same splash screen generated by the "About" command. It's bigger, for one thing. */
  public void splashScreenInit() {
    if (commandLine.writeIsSpecified()) return;

    ImageIcon myImage = new ImageIcon();
    try {
      myImage = new ImageIcon(FileUtil.findUrl(logoFile));
    }	catch (FileNotFoundException ex) {  }
 	
//    ImageIcon myImage = new ImageIcon(logoFile);
    // wacky but oboedit preference 1st call causing setting font which triggers gui stuff
    // which can deadlock loadingScreen, setOboSession triggers this when checks for
    // if reasoning, so do here to subdue later call
    Preferences.getPreferences();
    if (Config.inst().showLoadingScreen()) {
      LOG.debug("Creating splash screen...");
      // does splash screen belong in here?
      splashScreen = new SplashScreen(myImage); //,enable);
      loadingScreen = new LoadingScreen(SplashScreen.getPreferredDimension());
    
    //if (!enable) return;
//    splashScreen.setLocationRelativeTo(null);
//    splashScreen.setProgressMax(100);
//    splashScreen.setScreenVisible(true);
      loadingScreen.setScreenVisible(true);
      loadingScreen.setLocationRelativeTo(null); // centers panel on screen
    }
    else {
      LOG.info("Loading screen disabled by config, not showing");
    }
//    splashScreen.setProgress("Phenote version "+PhenoteVersion.versionString(), 0);
  }

  private void setProgress(int prog) {
    if (splashScreen != null) splashScreen.setProgress(prog);
    if (loadingScreen != null) loadingScreen.setProgress(prog);
  }
  private void setProgress(String msg, int progress) {
    setProgress(progress);
    setProgressMsg(msg);
//     if (splashScreen != null)  // ????
//       splashScreen.setProgress(msg,progress);
//     if (loadingScreen != null) {
//       loadingScreen.setMessageText(msg); // ??
//       loadingScreen.setProgress(msg,progress); // ??
//     }
  }
  private void setProgressMsg(String msg) {
    if (splashScreen != null) splashScreen.setMessage(msg); // ???
    if (loadingScreen != null) loadingScreen.setMessageText(msg);
  }
  
  void splashScreenDestruct() {
//    LOG.debug("splashScreenDestruct"); // DEL
    if (splashScreen!=null) splashScreen.setScreenVisible(false);
    if (loadingScreen!=null) loadingScreen.setScreenVisible(false);
    if (bePatientDialog != null) {
            bePatientDialog.setVisible(false);
            bePatientDialog.dispose();
    }
  }
  
  /** refactor? separate into doCmdLine and doConfig - cmd line should just set
      config file name - doConfig should actually read it */
  private void doCommandLineAndConfig(String[] args) {
    LOG.info("Parsing command-line args");
    try { commandLine.setArgs(args); } // sets config if specified
    catch (Exception e) {
      logErr("Command line arg parse failed: "+e); }//e.printStackTrace();

    // no config set from command line use default
    if (!Config.inst().isInitialized()) { 
      try { Config.inst().loadDefaultConfigFile(); }
      catch (ConfigException ce) { 
//        logErr("Failed to load requested configuration: "+ce+".  Loading fallback flybase config file instead.");
        logErr("Failed so far to load requested configuration " +  Config.inst().getDefaultFile() + ": "+ce);
        // We don't need to complain yet--there are still more things to try.
//	JOptionPane.showMessageDialog(null,"Couldn't find requested configuration file " + Config.inst().getDefaultFile(), "Can't find requested configuration file", JOptionPane.WARNING_MESSAGE);
        try {
          String masterConfig = Config.inst().getDefaultFile();
          String nameOfFile = FileUtil.getNameOfFile(masterConfig); 
          File dotPhenoteFile = new File(Config.inst().getDotPhenoteConfDir(), nameOfFile);
          logErr("Trying to find master copy of " + masterConfig + " in application directory and copy to " + dotPhenoteFile);
          Config.inst().findAndCopyConfigFile(masterConfig, dotPhenoteFile);
          logErr("Reloading " + dotPhenoteFile + " (which has now been updated from the master copy)");
          try { Config.inst().loadDefaultConfigFile(true); }  // This will keep the config from re-updating itself from the master URL that's in the config file (which is probably bad if we got to this point)
          catch (ConfigException c) {
            logErr("Uh oh, reload of " + dotPhenoteFile + " failed! "+c);
            JOptionPane.showMessageDialog(null,"Couldn't find a usable version of requested configuration file " + Config.inst().getDefaultFile() + ".\nYou will be prompted to choose a different configuration.", "Can't find requested configuration", JOptionPane.ERROR_MESSAGE);
            if (!Config.inst().resetMyConfig()) {
              String m = "Couldn't reset configuration!  Fatal error!  Please exit Phenote and try again later.";
              logErr(m);
              JOptionPane.showMessageDialog(null,m,"Couldn't reset configuration", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
        catch (ConfigException c) { 
//          logErr("Flybase default config has failed. We're hosed! "+c);
          // !! Make this a dialog
          logErr("Couldn't find a good version of requested config file anywhere!  Uh oh! "+c);
          JOptionPane.showMessageDialog(null,"Couldn't find a usable version of requested configuration file " + Config.inst().getDefaultFile() + ".\nYou will be prompted to choose a different configuration.", "Can't find requested configuration", JOptionPane.ERROR_MESSAGE);
          if (!Config.inst().resetMyConfig()) {
            String m = "Couldn't reset configuration!  Fatal error!  Please exit Phenote and try again later.";
            logErr(m);
            JOptionPane.showMessageDialog(null,m,"Couldn't reset configuration", JOptionPane.ERROR_MESSAGE);
          }
        }
      }
    }
    // now that config is done, add constraints from it
    Config.inst().loadConstraints();
  }

  public Frame getFrame() { return frame; }

  /** for old phenote - not phenote plus */
  private void makeMainWindow() {
    frame = new JFrame("Phenote "+PhenoteVersion.versionString()); 
    // just 1 group/tab
    if (getNumGroupTabs() < 2) {
      Group g = Config.inst().getDefaultGroup();
      frame.getContentPane().add(makeGroupPanel(g));
    }
    // > 1 group/tab
    else {
      JTabbedPane tabbedGroups = new JTabbedPane();
      for (Group g : getGroupTabs()) {
        JPanel p = makeGroupPanel(g);
        tabbedGroups.add(g.getTitle(),p);
      }
      frame.getContentPane().add(tabbedGroups);
    }
    
    MenuManager.createMenuManager(frame);
    frame.setPreferredSize(new Dimension(WINDOW_WIDTH,WINDOW_HEIGHT));
    if (standalone) // if stand alone exit java on window close
      frame.addWindowListener(new WindowExit());
    // this isnt showing up ???
    standardToolbar = new StandardToolbar();
    frame.add(standardToolbar, BorderLayout.NORTH);
    //frame.getContentPane().add(standardToolbar); doesnt work

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
    
    // EDITOR
    // shouldn't this be a local variable? why is it re-assigned for each group?
    this.tableController = new CharacterTableController(group.getName());
    
    // need to do different selection & edit mgrs
    FieldPanel groupFieldPanel =
      new FieldPanel(true,false,group.getName(), this.tableController.getSelectionModel());
    groupFieldPanel.setBorder(new EmptyBorder(10,10,10,10));
    this.tableController.setFieldPanelForUpdating(groupFieldPanel);
    // for testing - thats it
    if (group == null || group.getName().equals("default"))
      mainFieldPanel = groupFieldPanel;
    // groupFieldPanel gets added to innerSplitPane below...

    JComponent upperComponent = groupFieldPanel;

    // TERM INFO
    GridBagConstraints ugbc = GridBagUtil.makeFillingConstraint(0,0);
    if (showTermInfo(group)) {
      // INFO HISTORY
      JPanel infoHistoryPanel = new JPanel(new GridBagLayout());
      infoHistoryPanel.setBorder(new EmptyBorder(10,10,10,10));

//    termInfo = new TermInfo();
      termInfo = TermInfo2.inst();
      ugbc.weightx = 5;
      infoHistoryPanel.add(termInfo.getComponent(),ugbc);

      selectionHistory = SelectionHistory.inst();
      ugbc.gridx++;
      ugbc.weightx = 3;
      infoHistoryPanel.add(selectionHistory.getComponent(),ugbc); 
      //odd but explicitly setting min causes divider to not get stuck on window resize
      infoHistoryPanel.setMinimumSize(new Dimension(100,100));
      JSplitPane innerSplitPane =
        new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,true,groupFieldPanel,infoHistoryPanel);
      // doesnt work???
      //innerSplitPane.setDividerLocation(showTermInfo(group) ? 0.63 : 1.0);
      int div = (int)(showTermInfo(group) ? 0.63 * WINDOW_WIDTH : WINDOW_WIDTH);
      innerSplitPane.setDividerLocation(div);
      upperComponent = innerSplitPane;
    }
    
    
    
    // TABLE
    if (showTable(group))  {
      JPanel ctp =  this.tableController.getCharacterTablePanel();
      JSplitPane outerSplitPane =
        new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, upperComponent,ctp);
      mainPanel.add(outerSplitPane);
      outerSplitPane.setDividerLocation((int)(0.50 * WINDOW_HEIGHT)); // .4 -> .5
      //outerSplitPane.setDividerLocation(0.3); // proportion doesnt work til draw?
    }
    else {
      mainPanel.add(upperComponent);
    }
    return mainPanel;
  }

  private boolean showTermInfo(Group g) { return showString(g,"TermInfo"); }
  private boolean showTable(Group g) { return showString(g,"Table"); }
  private boolean showEditor(Group g) { return showString(g,"Editor"); }
  private boolean showString(Group g, String gui) {
    List<?> shows = g.getShow();
    if (shows == null) return true;
    return shows.contains(gui); // case insensitive?
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
      LOG.error(m);
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
  
  public static boolean isRunningOnMac() {
    return (System.getProperty("mrj.version") != null);
  }

  private static void logInfo(String m) {
    // stdout just in case logger aint jibin
    System.out.println(m);
    LOG.info(m);
  }

  private static void logErr(String m) {
    // stdout just in case logger aint jibin
    System.out.println(m); // err?
    LOG.error(m);
  }
  

}

//   /** args is most likely null if not called from command line */
//   public void initConfig(String[] args) {
//     // gets config file from command line & loads - if no config file 
//     // loads default. should actually put that logic here.
//     doCommandLine(args); // load config file
//   }

