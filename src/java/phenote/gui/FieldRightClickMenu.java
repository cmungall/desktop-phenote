package phenote.gui;

//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import phenote.gui.actions.CopyAction;


public class FieldRightClickMenu extends JPopupMenu {
	//this menu does the basic editing functionality cut/copy/paste 
	//for free text fields
	//this in some way needs to link up to the undo/redo transaction 
	//history i think.
	
	public FieldRightClickMenu(JTextComponent field) {
		super();
		init(field);
	}

	private void init(JTextComponent field) {
//		Create the popup menu.
		JMenuItem menuItem;
		menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
		menuItem.setText("Default Copy");
		menuItem.setMnemonic(KeyEvent.VK_C);
		menuItem.setEnabled(true);
		add(menuItem);
		menuItem = new JMenuItem(new CopyAction(field));
		add(menuItem);
		menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
		menuItem.setText("Cut");
		menuItem.setEnabled(true);
		menuItem.setMnemonic(KeyEvent.VK_X);
		add(menuItem);
//		menuItem = new JMenuItem();
		menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
		//		menuItem.addActionListener(new PasteActionListener());
		menuItem.setText("Paste");
		menuItem.setEnabled(true);
		menuItem.setMnemonic(KeyEvent.VK_V);
		add(menuItem);
	}
}
