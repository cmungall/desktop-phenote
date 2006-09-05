package phenote.gui;

// package phenote.main?

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.metal.MetalLookAndFeel;

import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.dataadapter.OntologyDataAdapter;

public class Phenote {

  private static final String VERSION = "0.7.6 dev";
  //private static final String DEFAULT_CONFIG_FILE = Config.DEFAULT_CONFIG_FILE;

  private CharacterTablePanel characterTablePanel;
  private TermPanel termPanel;
  private static Phenote phenote;
  private TermInfo termInfo;
  

  public static void main(String[] args) {
    System.out.println("This is Phenote version "+VERSION);
    // default mac lok & feel is "Mac OS X", but the JComboBox is buggy
    try {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    }
    catch (UnsupportedLookAndFeelException e) {
      System.out.println("Failed to set to Java/Metal look & feel");
    }
    //System.out.println("sys CONFIG prop "+System.getProperty("CONFIG"));
    phenote = getPhenote();
    phenote.initConfig(args);
    phenote.initOntologies();
    phenote.initGui();
  }

  /** private constructor -> singleton */
  private Phenote() {}

  /** args is most likely null if not called from command line */
  public void initConfig(String[] args) {
    // gets config file from command line & loads - if no config file 
    // loads default. should actually put that logic here.
    doCommandLine(args); // load config file
  }

  public void initOntologies() {
    OntologyDataAdapter oda = new OntologyDataAdapter(); // singleton?
    oda.loadOntologies(); // loads up OntologyManager
  }
  
  public void initGui() {
    makeWindow();
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
  private void doCommandLine(String[] args) {
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
      System.out.println("EXITING! Fatal error in config file: "+e.getMessage());
      e.printStackTrace();
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

  private void makeWindow() {
    JFrame frame = new JFrame("Phenote "+VERSION); // this may be changed to applet...
    frame.getContentPane().add(makeMainPanel());
    MenuManager.createMenuManager(frame);
    frame.setPreferredSize(new Dimension(1000,550));
    frame.pack();
    frame.setVisible(true);
  }

  /** main panel contains TermPanel CharTablePanel & TermInfo */
  private JPanel makeMainPanel() {
    JPanel mainPanel = new JPanel(new GridBagLayout()); // ??
    
    JPanel termAndTablePanel = new JPanel();
    BoxLayout bl = new BoxLayout(termAndTablePanel,BoxLayout.Y_AXIS);
    termAndTablePanel.setLayout(bl);

    //termAndInstancePanel.add(makeTermPanel());
    termPanel = new TermPanel();
    termAndTablePanel.add(termPanel);

    characterTablePanel = new CharacterTablePanel(termPanel);
    // eventually switch to event listener - no explicit connection...
    //termPanel.setCharacterTablePanel(characterTablePanel);
    termAndTablePanel.add(characterTablePanel);

    GridBagConstraints gbc = GridBagUtil.makeConstraint(0,0,5,5);
    mainPanel.add(termAndTablePanel,gbc);

    termInfo = new TermInfo(termPanel);
    ++gbc.gridx;  // ??
    gbc.anchor = GridBagConstraints.NORTHWEST;
    mainPanel.add(termInfo.getComponent(),gbc);

    return mainPanel;
  }

  public static Phenote getPhenote() {  // singleton
    if (phenote == null) phenote = new Phenote();
    return phenote;
  }
  // These methods are actually for TestPhenote
  TermPanel getTermPanel() { return termPanel; }
  TermInfo getTermInfo() { return termInfo; }
  CharacterTablePanel getCharacterTablePanel() { return characterTablePanel; }
}


//     SearchPanel searchPanel = new SearchPanel(termPanel);
//     mainPanel.add(searchPanel);
//     termPanel.setSearchPanel(searchPanel);
