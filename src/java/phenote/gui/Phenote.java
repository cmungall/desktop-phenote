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
import phenote.dataadapter.OntologyDataAdapter;

public class Phenote {

  private static final String VERSION = "0.7.4 dev";
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
      if we need to get more sophisticated */
  private void doCommandLine(String[] args) {
    String configFile = getConfigFileFromCommandLine(args);
    if (configFile == null)
      configFile = Config.DEFAULT_CONFIG_FILE;
    Config.inst().setConfigFile(configFile); // causes parse of file
  }

  private String getConfigFileFromCommandLine(String args[]) {
    // need 2 args
    if (args == null || args.length <= 1) return null;
    String firstArg = args[0];
    if (!firstArg.equals("-c")) return null;
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
