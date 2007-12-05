package phenote.gui.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.bbop.swing.DynamicActionMenuItem;
import org.bbop.swing.DynamicMenu;

import phenote.config.Config;
import phenote.edit.EditManager;
import phenote.gui.actions.RedoAction;
import phenote.gui.actions.ResponderChainAction;
import phenote.gui.actions.UndoAction;


//import phenote.gui.prefswindow.PrefsWindowController; ???

public class EditMenu extends DynamicMenu {

  private JMenuItem editMenuItem;
  

  public EditMenu() {
    super("Edit");
    init();
  }

  private void init() {
    EditActionListener actionListener = new EditActionListener();

    editMenuItem = new DynamicActionMenuItem(new UndoAction());
    add(editMenuItem);
    editMenuItem = new DynamicActionMenuItem(new RedoAction());
    add(editMenuItem);
    
    addSeparator();
    

    JMenuItem cut = new JMenuItem();
    Action cutAction = new ResponderChainAction("cut", "Cut");
    cutAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    cut.setAction(cutAction);
    add(cut);

    JMenuItem copy = new JMenuItem();
    Action copyAction = new ResponderChainAction("copy", "Copy");
    copyAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    copy.setAction(copyAction);
    add(copy);

    JMenuItem paste = new JMenuItem();
    Action pasteAction = new ResponderChainAction("paste", "Paste");
    pasteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    paste.setAction(pasteAction);
    add(paste);
    
    addSeparator();
    
    JMenuItem newChar = new JMenuItem("Add Annotation");
    Action newAction = new ResponderChainAction("addNewCharacter", "Add Annotation");
    newChar.setAction(newAction);
    add(newChar);
    
    JMenuItem duplicateChar = new JMenuItem();
    Action duplicateAction = new ResponderChainAction("duplicateSelectedCharacters", "Duplicate Annotation");
    duplicateChar.setAction(duplicateAction);
    add(duplicateChar);

    JMenuItem deleteChar = new JMenuItem();
    Action deleteAction = new ResponderChainAction("deleteSelectedCharacters", "Delete Annotation");
    deleteChar.setAction(deleteAction);
    add(deleteChar);
    
    JMenuItem selectAll = new JMenuItem("Select All");
    selectAll.setActionCommand("selectAll");
    selectAll.addActionListener(actionListener);
    selectAll.setMnemonic( 'A' );

    //set all to disabled until they are working.
    setEnabled(true);

 }

  private class EditActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      int selectRow = 1;
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
