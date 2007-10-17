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

public class SaveAsFileAction extends AbstractAction {
	//need a property change listener to see if the file has been modified since 
	//last saved.  be smart!
	public SaveAsFileAction() {
		super("Save As...", new ImageIcon("images/Save24.gif"));
		putValue(SHORT_DESCRIPTION, "Save File Dialog"); //tooltip text
		putValue(NAME, "Save As...");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
    setEnabled(Config.inst().hasDataAdapters());
	}
	public void actionPerformed(ActionEvent e) {
		//if its a button, then do one thing
		//if its a menu item, do something else
		//log this action
    if (!Config.inst().hasDataAdapters()) {
      System.out.println("no file data adapter to load/save with");
      return;
    }
		LoadSaveManager.inst().saveData();
		//log this action
		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
	//	return action;
}  


