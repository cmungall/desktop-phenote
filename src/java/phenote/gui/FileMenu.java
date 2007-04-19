

package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.LoadSaveManager;
import phenote.config.Config;
import phenote.config.ConfigFileQueryGui;
import phenote.config.ConfigException;
//import phenote.gui.prefswindow.PrefsWindowController; ???

class FileMenu extends JMenu {

  private JMenuItem loadMenuItem;

  FileMenu() {
    super("File");
    init();
  }

  private void init() {
    LoadSaveActionListener actionListener = new LoadSaveActionListener();

    JMenuItem newData = new JMenuItem("New");
    newData.setEnabled(Config.inst().hasDataAdapters());
    newData.setActionCommand("new");
    newData.addActionListener(actionListener);
    add(newData);
    
    loadMenuItem = new JMenuItem("Open...");
    loadMenuItem.setEnabled(Config.inst().hasDataAdapters());
    loadMenuItem.setActionCommand("load");
    loadMenuItem.addActionListener(actionListener);
    add(loadMenuItem);

    JMenuItem save = new JMenuItem("Save As...");
    save.setEnabled(Config.inst().hasDataAdapters());
    save.setActionCommand("save");
    save.setMnemonic('s');
    save.addActionListener(actionListener);
    add(save);

//    JMenuItem export = new JMenuItem("Export...");
//    export.setEnabled(Config.inst().hasDataAdapters());
//    export.setActionCommand("export");
//    export.addActionListener(actionListener);
//    add(export);

//    JMenuItem loadConfig = new JMenuItem("Load Configuration");
//    loadConfig.addActionListener(new ConfigActionListener());
//    add(loadConfig);
    
    addSeparator();
    
    JMenuItem exit = new JMenuItem("Exit");
    exit.setEnabled(Config.inst().hasDataAdapters());
    exit.setActionCommand("exit");
    exit.addActionListener(new ExitActionListener());
    add(exit);

  }

  private class LoadSaveActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (!Config.inst().hasDataAdapters()) {
        System.out.println("no file data adapter to load/save with");
        return;
      }
      if (e.getActionCommand().equals("new")) {
    	  LoadSaveManager.inst().saveData();  //will need to make sure to save first!
    	  LoadSaveManager.inst().newData();
      } else if (e.getActionCommand().equals("load"))
    	  LoadSaveManager.inst().loadData();
      	else if (e.getActionCommand().equals("save"))
      	  LoadSaveManager.inst().saveData();
      	else if (e.getActionCommand().equals("export"))
    	  LoadSaveManager.inst().exportData();
    }
  }

  private class ConfigActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      // 1st step just set my-phenote.cfg & user restart phenote
      // eventually reconfigure phenote in same session, cfg,obo,gui
      boolean showCancel = true;
      try {
        String cfg = ConfigFileQueryGui.queryUserForConfigFile(showCancel);
        if (cfg != null && !cfg.equals(""))
          Config.writeMyPhenoteDefaultFile(cfg);
        String m = "You must restart phenote for new config to take effect";
        JOptionPane.showMessageDialog(null,m,"Please restart",
                                      JOptionPane.INFORMATION_MESSAGE);
      }
      catch (ConfigFileQueryGui.CancelEx ex) {} // its cancelled do nothing
      catch (ConfigException x) {
        String m = "Failed to change configuration "+x.getMessage();
        JOptionPane.showMessageDialog(null,m,"Config error",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  
  private class ExitActionListener implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
	    	if (e.getActionCommand().equals("exit")) {
	    	  String m = "Are you sure you want to quit?";
	    	  JOptionPane.showMessageDialog(null, m, "Phenote Exit",
	    			  JOptionPane.INFORMATION_MESSAGE);
    		  System.exit(0);
	    	}
	    }
  }	


  // for testing
  public void clickLoad() {
    loadMenuItem.doClick();
  }
}
