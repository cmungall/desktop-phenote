package phenote.config;

// this should be moved to gui - actually i think ConfigGui may more or less replace it?

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.swing.AbstractAction;
import javax.swing.border.EtchedBorder;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import phenote.gui.GridBagUtil;
import phenote.util.FileUtil;

import phenote.config.ProxyDialog;

public class ConfigFileQueryGui {

  //private String selection;
  private String selectedFile;
  private JDialog dialog;
  //private boolean okPressed = false;
  private ButtonGroup buttonGroup;
  private boolean hasCancelButton = false;
  private boolean isCancelled = false;
  private static final Logger LOG = Logger.getLogger(ConfigFileQueryGui.class);

  public static String queryUserForConfigFile() {
    try { return queryUserForConfigFile(false); }
    catch (CancelEx e) {return null;} // shouldnt happen with cancel false
  }

  public static String queryUserForConfigFile(boolean doCancel) throws CancelEx {
    ConfigFileQueryGui c = new ConfigFileQueryGui(doCancel);
    return c.queryUser();
  }

  private ConfigFileQueryGui(boolean hasCancelButton) {
    this.hasCancelButton = hasCancelButton;
  }

  private String queryUser() throws CancelEx {
    makeQueryDialog();
    if (isCancelled) throw new CancelEx();
    return selectedFile; 
  }

  public static class CancelEx extends Exception {}

  private void makeQueryDialog() {

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new GridBagLayout());
    mainPanel.setBackground(Color.white);

    JLabel text = new JLabel("Choose a configuration for Phenote: ");
    int center = GridBagConstraints.CENTER;
    GridBagConstraints gbc = GridBagUtil.makeAnchorConstraint(0,0,center);
    gbc.gridwidth=3;
    mainPanel.add(text,gbc);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    //    buttonPanel.setBackground(Color.white);
    buttonPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
    buttonGroup = new ButtonGroup();
    boolean doFirst = false;
    String currentConfig = null;
    try { currentConfig = Config.inst().getMyPhenoteConfigString(); }
    catch (IOException e) { doFirst = true; }
    for (String cfg : getConfigNames()) {
      JRadioButton b = new JRadioButton(new BtnAction(cfg)); // makes display name
      b.setBackground(Color.white);
      buttonPanel.add(b);
      // select current myphenote if exists, else select first
      if ( (currentConfig != null && cfg.equals(currentConfig)) 
          || (currentConfig == null && doFirst) ) {
        b.setSelected(true);
        selectedFile = cfg;
        doFirst = false;
      }
      buttonGroup.add(b);
    }
    ++gbc.gridy;
    mainPanel.add(buttonPanel,gbc);

    JButton proxyButton = new JButton("Set proxy...");
    gbc.gridwidth=1;
    ++gbc.gridy;
    mainPanel.add(proxyButton,gbc);
    proxyButton.addActionListener( new ActionListener() {
		    public void actionPerformed(ActionEvent evt) {
			    ProxyDialog pd = new ProxyDialog(null);
			    pd.setVisible(true);
		    }
	    }
	    );

    JButton ok = new JButton("OK");
    ++gbc.gridx;
    mainPanel.add(ok,gbc);
    ok.addActionListener(new OkActionListener());

    if (hasCancelButton) {
      JButton cancel = new JButton("Cancel");
      cancel.addActionListener(new CancelActionListener());
//      mainPanel.add(cancel,gbc);
    }
//    JButton info = new JButton("Info...");
//    info.addActionListener(new InfoActionListener());
//    ++gbc.gridx;
//    mainPanel.add(info,gbc);

    Frame f = phenote.main.Phenote.getPhenote().getFrame();
    // true -> modal -> this is crucial! 
    dialog = new JDialog(f,true); //"Choose Configuration",true);
    dialog.setLayout(new GridBagLayout());
    dialog.setTitle("Phenote Configuration");
    JScrollPane scroll = new JScrollPane(mainPanel);
    // shouldnt take up whole screen - tool bars get in the way and dont know if
    // tool bar is on top or bottom - so subtract 100
    int height = Math.min(getScreenHeight()-100,900);
//    System.out.println("Phenote config window height: " + height);  // DEL
    scroll.setPreferredSize(new Dimension(300,height));
    scroll.setMinimumSize(new Dimension(300,(int)(height*0.7))); // just in case
    //mainPanel.setPreferredSize(new Dimension(200,300));
//    mainPanel.setPreferredSize(new Dimension(300,height+50));
//    mainPanel.setPreferredSize(buttonPanel.getSize());
    mainPanel.validate();
    dialog.add(scroll);

//    dialog.add(info);
//    dialog.add(cancel);

    dialog.setPreferredSize(new Dimension(315,height+60));
    dialog.pack();
    centerOnScreen(dialog);
    dialog.addWindowListener(new WindowCancel());
    dialog.setVisible(true);
  }
  
  private void updateInfoPanel() {
  	
  }

  private class OkActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      //selection = g.getSelection().getActionCommand(); 
//	    String selectedFile = (String)(buttonGroup.getSelection()).getValue(); // DEL
      //okPressed = true;
//	    System.out.println("ok pressed in ok action listener--selectedfile = " + selectedFile); // DEL
      dialog.dispose();
    }  
  }
  private class CancelActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      isCancelled = true;
      
      dialog.dispose();
    }
  }
  private class InfoActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
    	System.out.println("info button pressed");
    	String m = "This feature will display some basic information about \na configuration. " +
    			"It is not yet enabled.";
      JOptionPane.showMessageDialog(null, m, "Config Info",
              JOptionPane.INFORMATION_MESSAGE);
    	return;
    }
  }
  private class WindowCancel extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      // should this do a System.exit if we are at startup?
      if (hasCancelButton)
        isCancelled = true;
      dialog.dispose();
    }
  }

  private class BtnAction extends AbstractAction {
    private String configFilename;
    BtnAction(String configFilename) {
      //String display = makeDisplayFromFile(configFilename);
      super(makeDisplayFromFile(configFilename));
      //putValue("filename",configFilename);
      this.configFilename = configFilename;
      updateInfoPanel();
    }
    public void actionPerformed(ActionEvent e) {
      selectedFile = configFilename;
      LOG.debug("User selected config file " + selectedFile);
    }
  }
  private String makeDisplayFromFile(String f) {
    f = f.replaceAll(".cfg","");
    f = f.replaceAll("-"," ");
    return f;
  }

  /** query conf directories in app conf, jar conf(webstart) and .phenote/conf 
   List? soreted alphabeitcally?
  these strings are the actual filenames as in name-with-dash.cfg */
  public static SortedSet<String> getConfigNames() {
    SortedSet<String> names = new TreeSet<String>();
    
    // JAR
    JarFile jar = ConfigFileQueryGui.getJarFile();
    if (jar != null) {
      Enumeration<JarEntry> en = jar.entries();
      while (en.hasMoreElements()) {
        String entry = en.nextElement().getName();
        if (entry.endsWith(".cfg")) {
          if (ConfigFileQueryGui.class.getResource("/"+entry) != null) {
            names.add(entry);
          }
        }
      }
    }
    
    // do app/distrib conf dir
    File appConf = new File("conf/");
    addCfgFromDir(appConf,names);

    // ~/.phenote/conf
    File dotPhenConf = FileUtil.getDotPhenoteConfDir();
    addCfgFromDir(dotPhenConf,names);

    return names;
  }
  
  private static JarFile getJarFile() {
    URL jarUrl = null;
    try {
      // !! This doesn't seem to be finding it!  I'm getting "Jar URL is file:/Users/nomi/Documents/workspace/Phenote/classfiles/"
      // (This is when testing in offline mode)
      jarUrl = ConfigFileQueryGui.class.getProtectionDomain().getCodeSource().getLocation();
      log().debug("Jar URL is " + jarUrl);
      return new JarFile(new File(jarUrl.toURI()));
    } catch (IOException e) {
      log().debug("No Phenote jar file found in " + jarUrl);
      if (jarUrl.toString().indexOf("/classfiles") > 0) {
        String jarfile = jarUrl.toString().substring(0, jarUrl.toString().indexOf("/classfiles")) + "/jars/phenote.jar";
        log().debug("Trying jar URL " + jarfile);
        try {
          jarUrl = new URL(jarfile);
        } catch (MalformedURLException mue) {
          log().debug("Jar URL didn't work: " + jarfile);
          return null;
        }
        try {
          return new JarFile(new File(jarUrl.toURI()));
        } catch (Exception uhoh) {
          log().debug("Couldn't convert jar URL to URI: " + jarUrl);
          return null;
        }
      }
    } catch (URISyntaxException e) {
      log().error("Could not convert jar URL to URI", e);
    }
    // from webstart toURI sometimes throws this, not on my machine but birn & zf???
    // so in this case revert to old way of getting this...
    catch (IllegalArgumentException x) {
      // JAR
      // should only go into jar if actually running from jar hmmmmm
      // will this work with webstart??? probably not
      try {
        BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");      
        URL codeBaseUrl = bs.getCodeBase(); // this is the url to phenote webstart
        String s = "jar:"+codeBaseUrl.toString()+"/jars/phenote.jar!/";
        log().debug("Jar URL from codebase is " + s);
        jarUrl = new URL(s);
        JarURLConnection juc = (JarURLConnection)jarUrl.openConnection();
        JarFile jar = juc.getJarFile();//new JarFile(jf);
        return jar;
        //} catch (IOException e) {}//System.out.println("io cant open phen jar "+e);}
      } catch (Exception e) {
        log().debug("Failed to get jar file: " + e.getMessage());
      }//log().debug("cant open phenote jar from webstart");} 
      //System.out.println("cant open phen jar "+e); } // ???
    }

    return null;
  }

  private static void addCfgFromDir(File confDir,SortedSet<String> names) {
    FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".cfg");}
      };
    File[] cfgFiles = confDir.listFiles(filter);
    if (cfgFiles == null) return; // null if nothing found
    for (File f : cfgFiles) {
      //System.out.println("adding cfg file "+f.getName()+" from dir "+confDir);
      names.add(f.getName());
    }
  }

  /** generic util? */
  private void centerOnScreen(Component c) {
    // toolbars are usually on bottom? should this shift up a bit?
    int x = (int)getScreenWidth()/2 - c.getWidth()/2;
    int y = (int)getScreenHeight()/2 - c.getHeight()/2;
    Point p = new Point(x,y);
    c.setLocation(p);
  }

  private int getScreenWidth() { return (int)getScreenSize().getWidth(); }
  private int getScreenHeight() { return (int)getScreenSize().getHeight(); }
  private Dimension getScreenSize() {
    return Toolkit.getDefaultToolkit().getScreenSize();
  }

  private static Logger log() {
    return Logger.getLogger(ConfigFileQueryGui.class);
  }
}


