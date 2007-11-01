package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import phenote.edit.EditManager;



//This is the action for undoing the last action
//to be used by menus & button items.

public class UndoAction extends AbstractAction {
	//need a property change listener to see if the file has been modified since 
	//last saved.  be smart!
	public UndoAction() {
		super("Undo", new ImageIcon("images/Undo24.gif"));
		putValue(SHORT_DESCRIPTION, "Undo Last Action"); //tooltip text
		putValue(NAME, "Undo");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_Z));
		setEnabled(true);
	}
	public void actionPerformed(ActionEvent e) {
		//log this action
	  EditManager.inst().undo();
//	  repaint();
		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
}  


