package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyEvent;
import java.awt.Point;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Toolkit;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
//import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
//import javax.swing.text.DefaultEditorKit;



import phenote.gui.CharacterTablePanel;
import phenote.gui.CharacterTableModel;
import phenote.main.Phenote;



class TableRightClickMenu extends JPopupMenu {
	//currently this menu set can only copy free text, or ontology 
	//field content as free text.  it will need to get smart to the charfields

	TableRightClickMenu() {
		super();
		init();
	}

	private void init() {
//		Create the popup menu.
		JMenuItem menuItem;
		menuItem = new JMenuItem();
		menuItem.setText("Copy");
		menuItem.setMnemonic(KeyEvent.VK_C);
		menuItem.addActionListener(new CopyActionListener());
		add(menuItem);
		menuItem = new JMenuItem();
//		menuItem.addActionListener(new CutActionListener());
		menuItem.setText("Cut");
		menuItem.setEnabled(false);
		menuItem.setMnemonic(KeyEvent.VK_X);
		add(menuItem);
		menuItem = new JMenuItem();
//		menuItem.addActionListener(new PasteActionListener());
		menuItem.setText("Paste");
		menuItem.setEnabled(false);
		menuItem.setMnemonic(KeyEvent.VK_V);
		add(menuItem);
	}

	private class CopyActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			CharacterTablePanel table = Phenote.getPhenote().getCharacterTablePanel();
			Point coord = table.getTableCoord();
			int row = (int) coord.getX();
			int col = (int) coord.getY();
			String s = table.getCellString(row, col);
			//System.out.println("copied ("+row+","+col+") "+s);
			Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			//System.out.println("system clipboard="+c.getContents(null));
			StringSelection stringSelection = new StringSelection( s );
			c.setContents( stringSelection , null);
		}
	}

	private class PasteActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			return;
		}
	}
}
