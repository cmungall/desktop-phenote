package phenote.gui.menu;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.bbop.swing.DynamicMenu;

import phenote.config.Config;
import phenote.edit.EditManager;
import phenote.gui.actions.ResponderChainAction;

public class EditMenu extends DynamicMenu {

  private JMenuItem editMenuItem;
  

  public EditMenu() {
    super("Edit");
    init();
  }

  private void init() {
    EditActionListener actionListener = new EditActionListener();
    
    JMenuItem undo = new JMenuItem();
    Action undoAction = new ResponderChainAction("undo", "Undo");
    undoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    undo.setAction(undoAction);
    add(undo);
    
    JMenuItem redo = new JMenuItem();
    Action redoAction = new ResponderChainAction("redo", "Redo");
    redoAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
    redo.setAction(redoAction);
    add(redo);
    
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
    
    JMenuItem selectAll = new JMenuItem();
    Action selectAllAction = new ResponderChainAction("selectAll", "Select All");
    selectAllAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    selectAll.setAction(selectAllAction);
    add(selectAll);
    
    addSeparator();
    
    JMenuItem newChar = new JMenuItem();
    Action newAction = new ResponderChainAction("addNewCharacter", "Add Annotation");
    newAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() | InputEvent.SHIFT_MASK));
    newChar.setAction(newAction);
    add(newChar);
    
    JMenuItem duplicateChar = new JMenuItem();
    Action duplicateAction = new ResponderChainAction("duplicateSelectedCharacters", "Duplicate Annotation");
    duplicateAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    duplicateChar.setAction(duplicateAction);
    add(duplicateChar);

    JMenuItem deleteChar = new JMenuItem();
    Action deleteAction = new ResponderChainAction("deleteSelectedCharacters", "Delete Annotation");
    deleteAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    deleteChar.setAction(deleteAction);
    add(deleteChar);
    
    JMenuItem nextChar = new JMenuItem();
    Action nextAction = new ResponderChainAction("commitAndSelectNext", "Enter and Select Next");
    nextAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    nextChar.setAction(nextAction);
    add(nextChar);
    
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
