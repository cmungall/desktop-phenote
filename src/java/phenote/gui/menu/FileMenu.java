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
import phenote.dataadapter.LoadSaveManager;
import phenote.dataadapter.OntologyMakerI;
import phenote.gui.actions.MakeOntolAction;
import phenote.main.Phenote;

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
    openAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    add(new JMenuItem(openAction));

    add(new JSeparator());
    
    final Action saveAction = new AbstractAction("Save") {
      public void actionPerformed(ActionEvent e) {
        LoadSaveManager.inst().saveData(true);
      }
    };
    saveAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    add(new JMenuItem(saveAction));

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

    for (OntologyMakerI om : Config.inst().getOntMakers()) {
      JMenuItem m = new JMenuItem(new MakeOntolAction(om));
      add(m);
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
