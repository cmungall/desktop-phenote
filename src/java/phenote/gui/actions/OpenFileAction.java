package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import phenote.config.Config;
import phenote.dataadapter.LoadSaveManager;

//This is the action for opening files to be used by menus & button items.

public class OpenFileAction extends AbstractAction {
	public OpenFileAction() {
		super("Open", new ImageIcon("images/Open24.gif"));
		putValue(SHORT_DESCRIPTION, "Open a file..."); // tooltip text
		putValue(NAME, "Open");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
		// *****************JOHN--- if i set the breakpoint here, it never broke
		// boolean hasAdapters = Config.inst().hasDataAdapters();
		// setEnabled(hasAdapters);
	}
	
	@Override
	public boolean isEnabled() {
		return Config.inst().hasDataAdapters();
	}

	public void actionPerformed(ActionEvent e) {
		// if its a button, then do one thing
		// if its a menu item, do something else
		if (!Config.inst().hasDataAdapters()) {
			System.out.println("no file data adapter to load/save with");
			return;
		}
		LoadSaveManager.inst().loadData();

		System.out.println(e.getActionCommand().toString()
				+ " action selected by:\n  " + e);

	}
	// return action;
}
