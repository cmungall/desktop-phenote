package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.dataadapter.LoadSaveManager;
import phenote.util.FileUtil;

//This is the action for opening files to be used by menus & button items.

public class SaveAsFileAction extends AbstractAction {
	//need a property change listener to see if the file has been modified since 
	//last saved.  be smart!
	public SaveAsFileAction() throws FileNotFoundException {
		super("Save As...", new ImageIcon(FileUtil.findUrl("images/Save24.gif")));
		putValue(SHORT_DESCRIPTION, "Save File Dialog"); //tooltip text
//		putValue(NAME, "Save As...");
//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));

	}
	
	@Override
	public boolean isEnabled() {
		return Config.inst().hasDataAdapters();
	}
	
	public void actionPerformed(ActionEvent e) {
		//if its a button, then do one thing
		//if its a menu item, do something else
		//log this action
    if (!Config.inst().hasDataAdapters()) {
      log().error("no file data adapter to load/save with");
      return;
    }
		LoadSaveManager.inst().saveData();
		//log this action
		log().debug(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
	
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
	//	return action;
}  

