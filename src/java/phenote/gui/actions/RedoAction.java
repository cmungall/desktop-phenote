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


//This is the action for undoing the last action
//to be used by menus & button items.

public class RedoAction extends AbstractAction {
	//need a property change listener to see if the file has been modified since 
	//last saved.  be smart!
	public RedoAction() {
		super("Undo", new ImageIcon("images/Redo24.gif"));
		putValue(SHORT_DESCRIPTION, "Redo Last Action"); //tooltip text
		putValue(NAME, "Redo");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
		setEnabled(false);
	}
	public void actionPerformed(ActionEvent e) {
		//log this action
		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
}  


