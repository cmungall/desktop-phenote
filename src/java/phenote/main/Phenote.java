package phenote.main;

// package phenote.main?

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.ImageIcon;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.io.FileNotFoundException;



// import javax.swing.JTextField;
// import javax.swing.text.Keymap;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.OntologyDataAdapter;
import phenote.gui.CharacterTablePanel;
import phenote.gui.GridBagUtil;
import phenote.gui.MenuManager;
import phenote.gui.TermInfo;
import phenote.gui.SelectionHistory;
import phenote.gui.field.FieldPanel;
import phenote.gui.SplashScreen;

public class Phenote {

  private static final Logger LOG = Logger.getLogger(Phenote.class);
  private static boolean standalone = false; // default for servlet

  private CharacterTablePanel characterTablePanel;
  private FieldPanel fieldPanel;
  private static Phenote phenote;
  private TermInfo termInfo;
  private SelectionHistory selectionHistory;
  private CommandLine commandLine = CommandLine.inst();
  private JFrame frame;
  public SplashScreen splashScreen;
  private String logoFile = "src/java/phenote/images/phenote_logo.jpg";

  //  public static Keymap defaultKeymap;
  

  public static void main(String[] args) {
    standalone = true; // i think this is ok
    System.out.println("This is Phenote version "+PhenoteVersion.versionString());
    // default mac look & feel is "Mac OS X", but the JComboBox is buggy
    try {
//       JTextField t = new JTextField();
//       defaultKeymap = t.getKeymap();
//       System.out.println("default keymap "+defaultKeymap);
      //System.out.println("default shortcut "+java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
      UIManager.setLookAndFeel(new MetalLookAndFeel());
      //System.out.println("metal shortcut "+java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
//       t = new JTextField();
//       Keymap k = t.getKeymap();
//       System.out.println("metal keymap "+k);
    }
    catch (UnsupportedLookAndFeelException e) {
      System.out.println("Failed to set to Java/Metal look & feel");
    }
    phenote = getPhenote();
    phenote.splashScreenInit(); //initialize the splash screen;
    phenote.doCommandLine(args); // does config
    // put this is in a phenote.util.Log class? - get file from config - default?
    phenote.splashScreen.setProgress("Configuring...", 10);
    try { DOMConfigurator.configure(Config.inst().getLogConfigUrl()); }
    catch (FileNotFoundException e) { 
    	phenote.splashScreen.setProgress("bad file:"+e.getMessage(),10);
    	LOG.error(e.getMessage()); }
    phenote.splashScreen.setProgress("Initializing Ontologies...", 20);
    phenote.initOntologies();
    phenote.splashScreen.setProgress("Ontologies Initialized", 70);
    phenote.loadFromCommandLine();  // aft ontols, reads from cmd line if specified
    phenote.splashScreen.setProgress("Phenote Loaded", 100);
    if (phenote.commandLine.writeIsSpecified())
      phenote.writeFromCommandLine();
    else	// init gui if not writing (& reading) from cmd line
    {
    	phenote.initGui();
    	phenote.splashScreenDestruct();

    }
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
    makeWindow(); // ok this is silly
  }


  private void splashScreenInit() {
    ImageIcon myImage = new ImageIcon(logoFile);
    splashScreen = new SplashScreen(myImage);
    splashScreen.setLocationRelativeTo(null);
    splashScreen.setProgressMax(100);
    splashScreen.setScreenVisible(true);
    splashScreen.setProgress("Phenote version "+PhenoteVersion.versionString(), 0);
  }

  private void splashScreenDestruct() {
    splashScreen.setScreenVisible(false);
  }
  
  private void doCommandLine(String[] args) {
    try { commandLine.setArgs(args); } // sets config if specified
    catch (Exception e) { // no log yet - sys.out
      System.out.println("Command line read failed: "+e); e.printStackTrace();}
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


  public Frame getFrame() { return frame; }

  private void makeWindow() {
    // this may be changed to applet...
    frame = new JFrame("Phenote "+PhenoteVersion.versionString()); 
    frame.getContentPane().add(makeMainPanel());
    MenuManager.createMenuManager(frame);
    frame.setPreferredSize(new Dimension(1220,800)); //1100,700));
    if (standalone) // if stand alone exit java on window close
      frame.addWindowListener(new WindowExit());
    frame.pack();
    frame.setVisible(true);
  }

  /** for standalone & webstart - not for servlet as i think it will bring the
      whole servlet down */
  private class WindowExit extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      if (standalone)
        System.exit(0);
    }
  }

  /** main panel contains FieldPanel CharTablePanel & TermInfo 
      move this to gui? */
  private JPanel makeMainPanel() {
    JPanel mainPanel = new JPanel(new GridBagLayout()); // ??
    
    JPanel upperPanel = new JPanel(new GridBagLayout());
    //BoxLayout bl=new BoxLayout(upperPanel,BoxLayout.X_AXIS);upperPanel.setLayout(bl);

    GridBagConstraints ugbc = GridBagUtil.makeFillingConstraint(0,0);
    ugbc.weightx = 1;
    
    fieldPanel = new FieldPanel(); // field panel contains search params
    upperPanel.add(fieldPanel,ugbc);

    termInfo = new TermInfo();
    ugbc.gridx++;
    ugbc.weightx = 5;
    upperPanel.add(termInfo.getComponent(),ugbc);
    
    selectionHistory = SelectionHistory.inst();

    ugbc.gridx++;
    ugbc.weightx = 3;
    upperPanel.add(selectionHistory.getComponent(),ugbc);    

    //    ugbc.gridx++;
    //ugbc.weightx = 3;
    //upperPanel.add(selectionHistory.getComponent(),ugbc);
    //++gbc.gridx;  // ??
    //gbc.anchor = GridBagConstraints.NORTHWEST; // ??
    //mainPanel.add(termInfo.getComponent(),gbc);
    

    //GridBagConstraints gbc = GridBagUtil.makeConstraint(0,0,4,4); // x,y,hPad,vPad
//     double weightY = 1;
//     int fill = GridBagConstraints.BOTH;
//     int anchor = GridBagConstraints.WEST;
//     GridBagConstraints gbc = GridBagUtil.makeConstraint(0,0,1,1,weightY,4,4,fill,anchor);
    GridBagConstraints gbc = GridBagUtil.makeFillingConstraint(0,0);
    gbc.weighty = 1;
    mainPanel.add(upperPanel,gbc);

    characterTablePanel = new CharacterTablePanel();
    //termAndTablePanel.add(characterTablePanel);
    ++gbc.gridy; // ?
    gbc.weighty = 10;
    mainPanel.add(characterTablePanel,gbc);

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
  // These methods are actually for TestPhenote
  public FieldPanel getFieldPanel() { return fieldPanel; }
  public TermInfo getTermInfo() { return termInfo; }
  public CharacterTablePanel getCharacterTablePanel() { return characterTablePanel; }

}

//   /** args is most likely null if not called from command line */
//   public void initConfig(String[] args) {
//     // gets config file from command line & loads - if no config file 
//     // loads default. should actually put that logic here.
//     doCommandLine(args); // load config file
//   }

