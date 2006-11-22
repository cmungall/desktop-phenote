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
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.io.FileNotFoundException;

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
import phenote.gui.field.FieldPanel;

public class Phenote {

  //private static final String VERSION = "0.8.2 dev";
  private static final Logger LOG = Logger.getLogger(Phenote.class);
  private static boolean standalone = false; // default for servlet

  private CharacterTablePanel characterTablePanel;
  private FieldPanel fieldPanel;
  private static Phenote phenote;
  private TermInfo termInfo;
  private CommandLine commandLine = CommandLine.inst();
  private JFrame frame;
  

  public static void main(String[] args) {
    standalone = true; // i think this is ok
    System.out.println("This is Phenote version "+PhenoteVersion.versionString());
    // default mac lok & feel is "Mac OS X", but the JComboBox is buggy
    try {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    }
    catch (UnsupportedLookAndFeelException e) {
      System.out.println("Failed to set to Java/Metal look & feel");
    }
    phenote = getPhenote();
    //phenote.initConfig(args);
    phenote.doCommandLine(args); // does config
    // put this is in a phenote.util.Log class? - get file from config - default?
    try { DOMConfigurator.configure(Config.inst().getLogConfigUrl()); }
    catch (FileNotFoundException e) { LOG.error(e.getMessage()); }
    phenote.initOntologies();
    phenote.loadFromCommandLine();  // aft ontols, reads from cmd line if specified
    if (phenote.commandLine.writeIsSpecified())
      phenote.writeFromCommandLine();
    else // init gui if not writing (& reading) from cmd line
      phenote.initGui();
  }

  /** private constructor -> singleton */
  private Phenote() {}

//   /** args is most likely null if not called from command line */
//   public void initConfig(String[] args) {
//     // gets config file from command line & loads - if no config file 
//     // loads default. should actually put that logic here.
//     doCommandLine(args); // load config file
//   }

  public void initOntologies() {
    //OntologyDataAdapter oda = new OntologyDataAdapter(); // singleton?
    // loads up OntologyManager - non intuitive?
    OntologyDataAdapter.initialize(); 
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
    makeWindow();
  }

  private void doCommandLine(String[] args) {
    doCommandLineOld(args); // -c -i  --> move to CommandLine!
    try { commandLine.setArgs(args); } // no log yet - sys.out
    catch (Exception e) { System.out.println("Command line read failed"+e); }
  }

  /** for now just looking for '-c configFile.cfg', use command line package
      if we need to get more sophisticated 
      so if user has personal config file should override this - however maybe 
      there should be a distinction between initial config file and user made configs
      well really the initials are db/species specific - so could be 
      --initialConfig zf|fb|obd - actually da heck with that with entity chooser just
      put all 3 ontologies in one which is then the default (unspecified on cmd line)
      and -c file.cfg will load/overwrite that cfg into .phenote/my-phenote.cfg 
      (if it exists) - we can always add --init later if we need it 
      -c overwrites, -i doesnt -i is for initial startup of phenote */
  private void doCommandLineOld(String[] args) {
    String configFile = getConfigFileFromCommandLine(args);
    // if no config file specified then set default initial config file. this will be
    // overridden by a personal config file if it exists
    if (configFile == null)
      configFile = Config.DEFAULT_CONFIG_FILE;
    try {
      if (isOverwriteConfigFile(args))
        Config.inst().setOverwriteConfigFile(configFile); // causes parse of file->.phenote
      else
        Config.inst().setInitialConfigFile(configFile);
    } catch (ConfigException e) {
      LOG.fatal("EXITING! Fatal error in config file: "+e.getMessage());
      e.printStackTrace(); // log?
      System.exit(1);
    }
  }

  private boolean isInitialConfigFile(String args[]) {
    if (args == null || args.length < 2) return false;
    return args[0].equals("-i");
  }
  private boolean isOverwriteConfigFile(String args[]) {
    if (args == null || args.length < 2) return false;
    return args[0].equals("-c");
  }

  private String getConfigFileFromCommandLine(String args[]) {
    // need 2 args
    if (args == null || args.length < 2) return null;
    //String firstArg = args[0];
    if (!isInitialConfigFile(args) && !isOverwriteConfigFile(args)) return null;
    String configFile = args[1];
    return configFile;
  }

  public Frame getFrame() { return frame; }

  private void makeWindow() {
    // this may be changed to applet...
    frame = new JFrame("Phenote "+PhenoteVersion.versionString()); 
    frame.getContentPane().add(makeMainPanel());
    MenuManager.createMenuManager(frame);
    frame.setPreferredSize(new Dimension(1100,700));
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
    
    //JPanel termAndTablePanel = new JPanel();
    JPanel upperPanel = new JPanel();
    //BoxLayout bl = new BoxLayout(termAndTablePanel,BoxLayout.Y_AXIS);
    BoxLayout bl = new BoxLayout(upperPanel,BoxLayout.X_AXIS);
    upperPanel.setLayout(bl);

    fieldPanel = new FieldPanel();
    upperPanel.add(fieldPanel);



    termInfo = new TermInfo(); //fieldPanel);
    upperPanel.add(termInfo.getComponent());
    //++gbc.gridx;  // ??
    //gbc.anchor = GridBagConstraints.NORTHWEST; // ??
    //mainPanel.add(termInfo.getComponent(),gbc);
    

    GridBagConstraints gbc = GridBagUtil.makeConstraint(0,0,5,5);
    mainPanel.add(upperPanel,gbc);

    characterTablePanel = new CharacterTablePanel();
    //termAndTablePanel.add(characterTablePanel);
    ++gbc.gridy; // ?
    mainPanel.add(characterTablePanel,gbc);

    return mainPanel;
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


//     SearchPanel searchPanel = new SearchPanel(fieldPanel);
//     mainPanel.add(searchPanel);
//     fieldPanel.setSearchPanel(searchPanel);
