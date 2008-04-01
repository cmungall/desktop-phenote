

package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import phenote.config.Config;
import phenote.edit.EditManager;
import phenote.gui.actions.RedoAction;
import phenote.gui.actions.UndoAction;



//import phenote.gui.prefswindow.PrefsWindowController; ???

class EditMenu extends JMenu {

  private JMenuItem editMenuItem;
  

  EditMenu() {
    super("Edit");
    init();
  }

  private void init() {
    EditActionListener actionListener = new EditActionListener();

    editMenuItem = new JMenuItem(new UndoAction());
    add(editMenuItem);
    editMenuItem = new JMenuItem(new RedoAction());
    add(editMenuItem);
//    editMenuItem = new JMenuItem("Undo");
//     editMenuItem.setActionCommand("undo");
//     editMenuItem.setMnemonic( 'z' );
//    editMenuItem.addActionListener(actionListener);
    
    addSeparator();


    JMenuItem cut = new JMenuItem("Cut");
    cut.setActionCommand("cut");
    cut.addActionListener(actionListener);
    add(cut);

    JMenuItem copy = new JMenuItem("Copy");
    copy.setActionCommand("copy");
    copy.addActionListener(actionListener);
    add(copy);

    JMenuItem paste = new JMenuItem("Paste");
    paste.setActionCommand("paste");
    paste.addActionListener(actionListener);
    add(paste);
    
    addSeparator();
    
    JMenuItem newChar = new JMenuItem("Add Annotation");
    newChar.setActionCommand("new");
    newChar.setMnemonic(KeyEvent.VK_A);
    newChar.addActionListener(actionListener);
    add(newChar);
    
    JMenuItem copyChar = new JMenuItem("Copy Annotation");
    copyChar.setActionCommand("copyChar");
    copyChar.setMnemonic( 'C' );
    copyChar.addActionListener(actionListener);
    add(copyChar);

    JMenuItem deleteChar = new JMenuItem("Delete Annotation");
    deleteChar.setActionCommand("delete");
    deleteChar.setMnemonic( 'X' );
    deleteChar.addActionListener(actionListener);
    add(deleteChar);
    
    //set all to disabled until they are working.
    setEnabled(true);

 }

  private class EditActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (!Config.inst().hasDataAdapters()) {
        System.out.println("no file data adapter to load/save with");
        return;
      }
      else if (e.getActionCommand().equals("undo")) {
    	  EditManager.inst().undo();
    	  repaint();
      	}
      else if(e.getActionCommand().equals("new")) {
  		String m = "Add a row.";
		JOptionPane.showMessageDialog(null, m, "Phenote Help",
			JOptionPane.INFORMATION_MESSAGE);

//          selectRow = characterTablePanel.characterTableModel.addNewBlankRow();
//          characterTablePanel.scrollToNewLastRowOnRepaint = true;//scrollToLastRow(); // scroll to new row
      }
    }
  }


  // for testing
  public void clickLoad() {
    editMenuItem.doClick();
  }
}
