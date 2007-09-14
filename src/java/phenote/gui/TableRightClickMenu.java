package phenote.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;

import phenote.datamodel.CharFieldValue;



class TableRightClickMenu extends JPopupMenu {
	//currently this menu set can only copy free text, or ontology 
	//field content as free text.  it will need to get smart to the charfields
  
  private JTable table;
  private Point location;

	TableRightClickMenu(JTable aTable) {
		super();
		this.table = aTable;
		init();
	}

	private void init() {
//		Create the popup menu.
		JMenuItem menuItem;
		menuItem = new JMenuItem();
		menuItem.setText("Copy as Text");
		menuItem.setMnemonic(KeyEvent.VK_C);
		menuItem.addActionListener(new CopyTextActionListener());
		add(menuItem);
		menuItem = new JMenuItem();
		menuItem.setText("Copy Term");
		menuItem.setMnemonic(KeyEvent.VK_C);
		menuItem.setEnabled(true);
		menuItem.addActionListener(new CopyCharActionListener());
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
	
	public void show(Component invoker, int x, int y) {
	  this.location = new Point(x, y);
	  super.show(invoker, x, y);
	}
	
	private Object getValue() {
	  final int column = this.table.getTableHeader().columnAtPoint(this.location);
    final int row = this.table.rowAtPoint(this.location);
    System.out.println("Column and row: " + column + "," + row);
    return this.table.getValueAt(row, column);
	}
	
	private String getText() {
    Object s = this.getValue();
     if (s==null) {
        return ("");
      } else return s.toString();
  }
	
	private CharFieldValue getCharField() {
	  Object cfv = this.getValue();
	  if (cfv instanceof CharFieldValue) {
	    return (CharFieldValue)cfv;
	  } else {
	    return null;
	  }
	}

	private class CopyTextActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final String s = TableRightClickMenu.this.getText();
			System.out.println("Copy: " + s);
			final Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
			final StringSelection stringSelection = new StringSelection(s);
			c.setContents(stringSelection, null);
		}
	}

	private class CopyCharActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			CharFieldValue cfv = TableRightClickMenu.this.getCharField();
			String s="";
			if (cfv!=null)
				s = cfv.getName();
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
