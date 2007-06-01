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
import java.awt.Dimension;
import java.awt.Point;
//import java.awt.BorderLayout;
//import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;


import javax.jnlp.*;

import phenote.gui.GridBagUtil;
import phenote.util.FileUtil;

public class ConfigFileQueryGui {

  //private String selection;
  private String selectedFile;
  private JDialog dialog;
  private JPanel infoPanel;
  //private boolean okPressed = false;
  private ButtonGroup buttonGroup;
  private boolean hasCancelButton = false;
  private boolean isCancelled = false;

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
    java.awt.Frame f = phenote.main.Phenote.getPhenote().getFrame();
    // true -> modal -> this is crucial! 
    dialog = new JDialog(f,true); //"Choose Configuration",true);
    dialog.setLayout(new GridBagLayout());
    dialog.setTitle("Phenote Configuration");
    JLabel text = new JLabel("Please pick a configuration for Phenote: ");
    int center = GridBagConstraints.CENTER;
    GridBagConstraints gbc = GridBagUtil.makeAnchorConstraint(0,0,center);
    gbc.gridwidth=3;
    dialog.add(text,gbc);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    buttonGroup = new ButtonGroup();
    boolean doFirst = false;
    String currentConfig = null;
    try { currentConfig = Config.inst().getMyPhenoteConfigString(); }
    catch (IOException e) { doFirst = true; }
   for (String cfg : getConfigNames()) {
      JRadioButton b = new JRadioButton(new BtnAction(cfg));
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
    dialog.add(buttonPanel,gbc);
    JButton ok = new JButton("OK");
    gbc.gridwidth=1;
    ++gbc.gridy;
    dialog.add(ok,gbc);
    ok.addActionListener(new OkActionListener());
    if (hasCancelButton) {
      JButton cancel = new JButton("Cancel");
      cancel.addActionListener(new CancelActionListener());
      ++gbc.gridx;
      dialog.add(cancel,gbc);
    }
    JButton info = new JButton("Info...");
    info.addActionListener(new InfoActionListener());
    ++gbc.gridx;
    dialog.add(info,gbc);
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
      //String selectedFile = (String)(buttonGroup.getSelection().getValue("filename"));
      //okPressed = true;
      //System.out.println("ok pressed in ok action listener "+okPressed);
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
    }
  }
  private String makeDisplayFromFile(String f) {
    f = f.replaceAll(".cfg","");
    //f = f.replaceAll("-"," ");
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
        String entry = en.nextElement().getName();
        //System.out.println("entry: "+entry);
        if (entry.endsWith(".cfg")) {
          // test if jar if running off of jar basically
          //System.out.println("cfg "+entry +" res? "+ConfigFileQueryGui.class.getResource(entry)+ConfigFileQueryGui.class.getResource("/"+entry));
          if (ConfigFileQueryGui.class.getResource("/"+entry) != null) {
            //System.out.println("cfg that is in webstart phenote.jar "+entry);
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
      //System.out.println("adding cfg file "+f.getName()+" from dir "+confDir);
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

}

// silly me - all i needed to do was make the dialog modal! silly silly
//  WaitSwingWorkerThread waitSwingWorkerThread;
    // doesnt work - still spins
    //Thread dt = new DialogThread();
//     try { 
//       // this says cant do from EventDispatcherThread
//       //javax.swing.SwingUtilities.invokeAndWait(new DialogThread());
//       // this literally makes the computer blow up
//       //javax.swing.SwingUtilities.invokeLater(new DialogThread());
//     } //catch (InterruptedException e) { System.out.println("interrupted "+e); }
//     catch (Exception e) {  System.out.println("Exception: "+e); }
    //dt.start();
    //DialogSwingWorkerThread dt = new DialogSwingWorkerThread();
    //dt.start();
//     while (!okPressed) {
//       System.out.println("ok pressed? "+okPressed+" cur thread "+Thread.currentThread()+" dlg thread "+dt);
//       try { Thread.sleep(300); } catch (InterruptedException e) {}
//     } // sleep??
//    return selectedFile; //(String) dt.get(); //selectedFile;
//     waitSwingWorkerThread  = new WaitSwingWorkerThread();
//     waitSwingWorkerThread.start();
//     return (String)waitSwingWorkerThread.get();
//   private class DialogThread extends Thread {
//     public void run() {
//       makeQueryDialog();
//     }
//   }

//   private class WaitSwingWorkerThread extends phenote.gui.SwingWorker {
//     public Object construct() {
//       while (!okPressed) {
//         System.out.println("ok pressed? "+okPressed+" cur thread "+Thread.currentThread()+" this "+this+" sel "+selectedFile);
// //        try {
//         waitSwingWorkerThread.sleep(300);// } catch (InterruptedException e) {}
//       } // sleep??
//       return selectedFile;
//     }
//   }
  

//   private class DialogSwingWorkerThread extends phenote.gui.SwingWorker {
//     public Object construct() {
//       makeQueryDialog();
// //     while (!okPressed) {
// //       System.out.println("ok pressed? "+okPressed+" cur thread "+Thread.currentThread()+" this "+this);
// //       try { Thread.sleep(300); } catch (InterruptedException e) {}
// //     } // sleep??
//       return selectedFile;
//     }
//   }

//   private class PhenoteClassLoader extends ClassLoader {
//     private PhenoteClassLoader() {
//       super(ClassLoader.getSystemClassLoader());
//     }
//     protected String findLibrary(String lib) {
//       return super.findLibrary(lib);
//       //return this.getParent().findLibrary(lib);
//     }
//     //protected ClassLoader getParent() { return super.getParent(); }
//   }
