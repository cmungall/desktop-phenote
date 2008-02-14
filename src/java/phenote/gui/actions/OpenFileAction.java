package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.dataadapter.LoadSaveManager;
import phenote.util.FileUtil;

//This is the action for opening files to be used by menus & button items.

public class OpenFileAction extends AbstractAction {
	public OpenFileAction() throws FileNotFoundException {
		super("Open...", new ImageIcon(FileUtil.findUrl("images/Open24.gif")));
		putValue(SHORT_DESCRIPTION, "Open a file..."); // tooltip text
//		putValue(NAME, "Open...");
//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	}
		
	@Override
	public boolean isEnabled() {
		return Config.inst().hasDataAdapters();
	}

	public void actionPerformed(ActionEvent e) {
		// if its a button, then do one thing
		// if its a menu item, do something else
		if (!Config.inst().hasDataAdapters()) {
			log().error("no file data adapter to load/save with");
			return;
		}
		LoadSaveManager.inst().loadData();

		System.out.println(e.getActionCommand().toString()
				+ " action selected by:\n  " + e);

	}
	
  private Logger log() {
    return Logger.getLogger(this.getClass());
  }
  
	// return action;
}
