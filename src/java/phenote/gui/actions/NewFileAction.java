package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

/**
 * This is the action for creating a new annotation file to begin editing.
 * When a new annotation file is created, it should:<p>
 * <ul> 
 * <li> ask user if they want to save </li>
 * <li> close the current open annotation file </li>
 * <li> wipe the current annotations and create a new blank row</li>
 * </ul>
 * Eventually, user should be able to have >1 open annotation file<p> 
 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 */
public class NewFileAction extends AbstractAction {
	public NewFileAction() {
		super("New", new ImageIcon("images/New24.gif"));
		putValue(SHORT_DESCRIPTION, "Open a new Annotation File"); // tooltip text
		putValue(NAME, "New");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
	}
	
	@Override
	public boolean isEnabled() {
		return false;
//		return Config.inst().hasDataAdapters();
	}


	public void actionPerformed(ActionEvent e) {
//		 LoadSaveManager.inst().saveData();
// 	  LoadSaveManager.inst().newData();
		System.out.println(e.getActionCommand().toString()
				+ " action selected by:\n  " + e);

	}
}
