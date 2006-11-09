package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterI;
import phenote.dataadapter.LoadSaveManager;
import phenote.config.Config;
import phenote.gui.prefswindow.PrefsWindowController;

class FileMenu extends JMenu {

  private JMenuItem loadMenuItem;

  FileMenu() {
    super("File");
    init();
  }

  private void init() {
    LoadSaveActionListener actionListener = new LoadSaveActionListener();

    loadMenuItem = new JMenuItem("Load Data");
    loadMenuItem.setEnabled(Config.inst().hasDataAdapters());
    loadMenuItem.setActionCommand("load");
    loadMenuItem.addActionListener(actionListener);
    add(loadMenuItem);

    JMenuItem save = new JMenuItem("Save Data");
    save.setEnabled(Config.inst().hasDataAdapters());
    save.setActionCommand("save");
    save.addActionListener(actionListener);
    add(save);
  }

  private class LoadSaveActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (!Config.inst().hasDataAdapters()) {
        System.out.println("no data adapter to load with");
        return;
      }
      if (e.getActionCommand().equals("load"))
        LoadSaveManager.inst().loadData();

      else if (e.getActionCommand().equals("save"))
        LoadSaveManager.inst().saveData();
    }
  }

  // for testing
  public void clickLoad() {
    loadMenuItem.doClick();
  }
}
