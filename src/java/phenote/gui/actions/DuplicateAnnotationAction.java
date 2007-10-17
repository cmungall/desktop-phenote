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

public class DuplicateAnnotationAction extends AbstractAction {
	public DuplicateAnnotationAction() {
		super("Duplicate", null);
		putValue(SHORT_DESCRIPTION, "Duplicate Selected Annotation(s)"); //tooltip text
		putValue(NAME, "Duplicate");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
	}
	public void actionPerformed(ActionEvent e) {
		//if its a button, then do one thing
		//if its a menu item, do something else

		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
		//add in all the business for the table copy stuff;
	}

}  


