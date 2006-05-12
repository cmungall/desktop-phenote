package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterI;
import phenote.config.Config;

class FileMenu extends JMenu {

  private JMenuItem loadMenuItem;

  FileMenu() {
    super("File");
    init();
  }

  private void init() {
    LoadActionListener actionListener = new LoadActionListener();

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

  private class LoadActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (!Config.inst().hasDataAdapters()) {
        System.out.println("no data adapter to load with");
        return;
      }
      if (e.getActionCommand().equals("load"))
        getDataAdapter().load();

      else if (e.getActionCommand().equals("save"))
        getDataAdapter().commit(CharacterListManager.inst().getCharacterList());
    }
  }

  private DataAdapterI getDataAdapter() {
    return Config.inst().getSingleDataAdapter();
  }

  // for testing
  public void clickLoad() {
    loadMenuItem.doClick();
  }
}
