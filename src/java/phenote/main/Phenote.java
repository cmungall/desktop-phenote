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
import phenote.gui.SelectionHistory;
import phenote.gui.field.FieldPanel;

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
  

  public static void main(String[] args) {
    standalone = true; // i think this is ok
    System.out.println("This is Phenote version "+PhenoteVersion.versionString());
    // default mac look & feel is "Mac OS X", but the JComboBox is buggy
    try {
      UIManager.setLookAndFeel(new MetalLookAndFeel());
    }
    catch (UnsupportedLookAndFeelException e) {
      System.out.println("Failed to set to Java/Metal look & feel");
    }
    phenote = getPhenote();
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
    makeWindow();
  }

  private void doCommandLine(String[] args) {
    //doCommandLineOld(args); // -c -u  --> move to CommandLine!
    try { commandLine.setArgs(args); } // no log yet - sys.out
    catch (Exception e) { System.out.println("Command line read failed"+e); }
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
    
    selectionHistory = new SelectionHistory();
    ugbc.gridx++;
    ugbc.weightx = 3;
    upperPanel.add(selectionHistory.getComponent(),ugbc);
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

