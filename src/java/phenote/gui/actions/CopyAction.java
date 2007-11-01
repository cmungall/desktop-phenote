package phenote.gui.actions;

//import javax.swing.JFrame;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;




//This is the action for undoing the last action
//to be used by menus & button items.
//public class CopyAction extends AbstractAction {

public class CopyAction extends AbstractAction  {
	//need a property change listener to see if the file has been modified since 
	//last saved.  be smart!
	
	JComponent textComponent;
	
	public CopyAction(JTextComponent source) {
		super("Copy", new ImageIcon("images/Copy24.gif"));
		init();
		textComponent = source;
		System.out.println("class="+textComponent.getClass());
	}
	
	
	public CopyAction() {
		super("Copy", new ImageIcon("images/Copy24.gif"));
		init();
//		super("Copy", new ImageIcon("images/Copy24.gif"));
//		putValue(SHORT_DESCRIPTION, "Copy Selection"); //tooltip text
//		putValue(NAME, "Copy");
//		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
//		setEnabled(true);
		//find the currently selected object in the system, and then 
		//call the other constructor?
	}
	private void init() {
		putValue(SHORT_DESCRIPTION, "Copy Selection"); //tooltip text
		putValue(NAME, "Copy");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_C));
	}
	
	public void actionPerformed(ActionEvent e) {
			JTextComponent tc = (JTextComponent)textComponent;
			tc.copy();
			String s= tc.getSelectedText();

		//different cases:
		//if this is called from the spreadsheet
		//if this is called from term info
		//if this is called from fields
		//different copy results if from free text, or ontology, etc.
//		final String s = getValue();
//		final Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
//		final StringSelection stringSelection = new StringSelection(s);
//		System.out.println("Copied: "+s);
		//log this action
		System.out.println(e.getActionCommand().toString()+" action selected by:\n  "+ e);
	}
}  


