package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import javax.swing.ImageIcon;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.*;
import java.awt.event.*;

import phenote.dataadapter.LoadSaveManager;
import phenote.config.Config;



//This is the action for opening files to be used by menus & button items.

public class OpenFileAction extends AbstractAction {
	public OpenFileAction() {
		super("Open", new ImageIcon("images/Open24.gif"));
		putValue(SHORT_DESCRIPTION, "Open a file..."); //tooltip text
		putValue(NAME, "Open");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
    setEnabled(Config.inst().hasDataAdapters());
	}
	public void actionPerformed(ActionEvent e) {
		//if its a button, then do one thing
		//if its a menu item, do something else
    if (!Config.inst().hasDataAdapters()) {
      System.out.println("no file data adapter to load/save with");
      return;
    }
	  LoadSaveManager.inst().loadData();

		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);

	}
//	return action;
}  


