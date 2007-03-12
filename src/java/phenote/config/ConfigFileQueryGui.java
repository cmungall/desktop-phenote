package phenote.config;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.IOException;
import java.net.URL;
import java.net.JarURLConnection;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Point;
//import java.awt.BorderLayout;
//import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;


import javax.jnlp.*;

import phenote.gui.GridBagUtil;
import phenote.util.FileUtil;

class ConfigFileQueryGui {

  //private String selection;
  private String selectedFile;
  private JDialog dialog;
  private boolean okPressed = false;
  private ButtonGroup buttonGroup;

  static String queryUserForConfigFile() {
    ConfigFileQueryGui c = new ConfigFileQueryGui();
    return c.queryUser();
  }

  private String queryUser() {
    dialog = new JDialog(); //"Choose Configuration",true);
    //dialog.setPreferredSize(new Dimension(300,400));
    //dialog.setLayout(new BoxLayout(dialog,BoxLayout.Y_AXIS));
    //dialog.setLayout(new GridLayout(0,1));
    //dialog.setLayout(new BorderLayout());
    dialog.setLayout(new GridBagLayout());
    dialog.setTitle("Phenote Configuration");
    // set layout?
    JLabel text = new JLabel("Please pick a configuration for Phenote: ");
    int center = GridBagConstraints.CENTER;
    GridBagConstraints gbc = GridBagUtil.makeAnchorConstraint(0,0,center);
    dialog.add(text,gbc);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    buttonGroup = new ButtonGroup();
    boolean first = true;
    for (String cfg : getConfigNames()) {
      JRadioButton b = new JRadioButton(new BtnAction(cfg));
      buttonPanel.add(b);
      if (first) {
        b.setSelected(true);
        selectedFile = cfg;
        first = false;
      }
      buttonGroup.add(b);
    }
    ++gbc.gridy;
    dialog.add(buttonPanel,gbc);
    JButton ok = new JButton("OK");
    ++gbc.gridy;
    dialog.add(ok,gbc);
    ok.addActionListener(new OkActionListener());
    dialog.pack();
    centerOnScreen(dialog);
    dialog.setVisible(true);
    //return selection; // ????
    while (!okPressed) {
      //System.out.println("ok pressed? "+okPressed);
      try { Thread.sleep(10); } catch (InterruptedException e) {}
    } // sleep??
    return selectedFile;
  }

  private class OkActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      //selection = g.getSelection().getActionCommand(); 
      //String selectedFile = (String)(buttonGroup.getSelection().getValue("filename"));
      okPressed = true;
      //System.out.println("ok pressed in ok action listener "+okPressed);
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
    }
    public void actionPerformed(ActionEvent e) {
      selectedFile = configFilename;
    }
  }
  private String makeDisplayFromFile(String f) {
    f = f.replaceAll(".cfg","");
    f = f.replaceAll("-"," ");
    return f;
  }

  /** query conf directories in app conf, jar conf(webstart) and .phenote/conf */
  private Set<String> getConfigNames() {

    Set<String> names = new LinkedHashSet<String>();

    // should only go into jar if actually running from jar hmmmmm
    // will this work with webstart??? probably not

    File jf = new File("jars/phenote.jar");

    try {
      BasicService bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");      
      URL codeBaseUrl = bs.getCodeBase(); // this is the url to phenote webstart
      String s = "jar:"+codeBaseUrl.toString()+"/jars/phenote.jar!/";
      URL jarUrl = new URL(s);
      JarURLConnection juc = (JarURLConnection)jarUrl.openConnection();
      JarFile jar = juc.getJarFile();//new JarFile(jf);
      Enumeration<JarEntry> en = jar.entries();
      while (en.hasMoreElements()) {
        System.out.println("entry: "+en.nextElement());
        String entry = en.nextElement().getName();
        if (entry.endsWith(".cfg")) {
          // test if jar if running off of jar basically
          //System.out.println("cfg "+entry +" res? "+ConfigFileQueryGui.class.getResource(entry)+ConfigFileQueryGui.class.getResource("/"+entry));
          if (ConfigFileQueryGui.class.getResource("/"+entry) != null) {
            System.out.println("cfg that is in webstart phenote.jar "+entry);
            names.add(entry);
          }
        }
      }
    } catch (IOException e) {}//System.out.println("io cant open phen jar "+e);}
    catch (Exception e) {} //System.out.println("cant open phen jar "+e); } // ???

    // do app/distrib conf dir
    File appConf = new File("conf/");
    addCfgFromDir(appConf,names);

    // ~/.phenote/conf
    File dotPhenConf = FileUtil.getDotPhenoteConfDir();
    addCfgFromDir(dotPhenConf,names);
    return names;
  }

  private void addCfgFromDir(File confDir,Set<String> names) {
    FilenameFilter filter = new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return name.endsWith(".cfg");}
      };
    File[] cfgFiles = confDir.listFiles(filter);
    if (cfgFiles == null) return; // null if nothing found
    for (File f : cfgFiles) {
      System.out.println("adding cfg file "+f.getName()+" from dir "+confDir);
      names.add(f.getName());
    }
  }

  /** generic util? */
  private void centerOnScreen(Component c) {
    Toolkit t = Toolkit.getDefaultToolkit();
    Dimension screen = t.getScreenSize();
    int x = (int)screen.getWidth()/2 - c.getWidth()/2;
    int y = (int)screen.getHeight()/2 - c.getHeight()/2;
    Point p = new Point(x,y);
    c.setLocation(p);
  }

  private class PhenoteClassLoader extends ClassLoader {

    private PhenoteClassLoader() {
      super(ClassLoader.getSystemClassLoader());
    }
    
    protected String findLibrary(String lib) {
      return super.findLibrary(lib);
      //return this.getParent().findLibrary(lib);
    }

    //protected ClassLoader getParent() { return super.getParent(); }

  }

}
