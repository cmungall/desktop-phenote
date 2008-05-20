package phenote.gui.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.bbop.framework.GUIManager;
import org.bbop.swing.DynamicMenu;

import phenote.config.Config;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.LoadSaveManager;
import phenote.dataadapter.OntologyMakerI;
import phenote.gui.actions.MakeOntolAction;
import phenote.main.Phenote;
import phenote.gui.GetInfo;

/**
 * This is the standard File menu for the main Phenote2 configuration.  It is
 * to include basic file and software operations:  New, Open, Save As, Exit.<p>
 * 
 * Future items will include (re)Save, Print, Properties?, Close, list of
 * recent annotation files (open recent).<p>
 * 
 * This menu has been adapted to work with the new bbop framework.<p>
 *
 * @author Mark Gibson
 * @author Nicole Washington
 * @author Jim Balhoff
 *
 */
public class FileMenu extends DynamicMenu {

  public FileMenu() {
    super("File");
    init();
  }

  @SuppressWarnings("serial")
  private void init() {

    // OPEN
    addOpenItem();

    // CLEAR
    addClearAnnotsItem();

    add(new JSeparator());
    
    // SAVE
    final Action saveAction = new AbstractAction("Save") {
      public void actionPerformed(ActionEvent e) {
        LoadSaveManager.inst().saveData(true);
      }
    };
    saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    add(new JMenuItem(saveAction));

    // SAVE AS
    final Action saveAsAction = new AbstractAction("Save As...") {
      public void actionPerformed(ActionEvent e) {
        if (!Config.inst().hasDataAdapters()) {
          log().error("No file data adapter to load/save with");
          return;
        }
        LoadSaveManager.inst().saveData();
      }
      @Override
      public boolean isEnabled() {
        return Config.inst().hasDataAdapters();
      }  
    };
    saveAsAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
    add(new JMenuItem(saveAsAction));



//  JMenuItem export = new JMenuItem("Export...");
//  export.setEnabled(Config.inst().hasDataAdapters());
//  export.setActionCommand("export");
//  export.addActionListener(actionListener);
//  add(export);
    
    // EXIT
    if (!Phenote.isRunningOnMac()) {
      // we don't want to add "Exit" to the File menu on Mac
      // instead there is "Quit" under the automatic Phenote menu
      addSeparator();

      JMenuItem exit = new JMenuItem("Exit") {
        @Override
        public void setEnabled(boolean b) {
          // TODO Auto-generated method stub
          super.setEnabled(b);
        }
      };
      exit.setEnabled(true);
      exit.setActionCommand("exit");
      exit.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          GUIManager.exit(0); 
        } });
      add(exit);
    }
    
    // PREFERENCES this doesnt work - throws null pointer - commenting out for now
    //addPreferencesItem();
    
    for (OntologyMakerI om : Config.inst().getOntMakers()) {
      JMenuItem m = new JMenuItem(new MakeOntolAction(om));
      add(m);
    }
    
  }

  /** Open Menu item, for getting new data */
  private void addOpenItem() {
    final Action openAction = new AbstractAction("Open...") {
      public void actionPerformed(ActionEvent e) {
        if (!Config.inst().hasDataAdapters()) {
          log().error("No file data adapter to load/save with");
          return;
        }
        LoadSaveManager.inst().loadData();
      }
      @Override
      public boolean isEnabled() {
        return Config.inst().hasDataAdapters();
      }  
    };
    
    openAction.putValue(Action.ACCELERATOR_KEY,getKeyStroke(KeyEvent.VK_O));
    add(new JMenuItem(openAction));

  }
  
  private KeyStroke getKeyStroke(int keyCode) {
    int modifiers =  Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    return KeyStroke.getKeyStroke(keyCode,modifiers);
  }

  /** Menu to clear out all annotations, start anew */
  private void addClearAnnotsItem() {
    JMenuItem clear = new JMenuItem("Clear All Annotations");
    clear.addActionListener(new ClearAnnots());
    add(clear);
  }

  /** this currently doesnt work - throws null pointer - commented out above */
  private void addPreferencesItem() {
    addSeparator();
    JMenuItem preferences = new JMenuItem("Preferences");
    preferences.addActionListener(new ActionListener() {
    	public void actionPerformed(ActionEvent e) {
    		GetInfo getInfo = new GetInfo();
//    		getInfo.setVisible(true);
//    		getInfo.setEnabled(true);
//    		getInfo.setAlwaysOnTop(true);
    	}
    });
    add(preferences);
  }
  

  /** clear out all annots - should this bring up a are you sure popup?
      also clears transactions */
  private class ClearAnnots implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      CharacterListManager.clearAnnotations(); // ??
    }
  }

  // for testing
  public void clickLoad() {
    //loadMenuItem.doClick();
  }

  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
}
