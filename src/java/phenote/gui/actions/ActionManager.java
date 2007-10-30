package phenote.gui.actions;
// --> phenote.gui.menu ??

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;


//This is to help centralize actions that appear in buttons, menus, etc.

public class ActionManager {
  
  private Action saveAction;

  private Action openAction;
  
  private static ActionManager singleton;

  public ActionManager() {}

  public static ActionManager inst() {
    if (singleton == null) {
      singleton = new ActionManager();
    }
    return singleton; 
    }
  
  public static void reset() {
    singleton = null;
  }
  
  public Action getAction(String actionName) {
    Action action=null;
  	if (actionName.equals("Save")) {
  		action = new SaveAction(actionName, new ImageIcon("images/Save24.gif"),
  				"Save as...", new Integer(KeyEvent.VK_S));
  		}
  	else if (actionName.equals("Open")) {
  		action = new SaveAction(actionName, new ImageIcon("images/Open24.gif"),
  				"Open file...", new Integer(KeyEvent.VK_O));
  	}
  	else if (actionName.equals("New")) {
  		action = new SaveAction(actionName, new ImageIcon("images/New24.gif"),
  			"New file...", new Integer(KeyEvent.VK_N));
  	}
  	return action;
  }
  
  public class SaveAction extends AbstractAction {
  		public SaveAction(String label, ImageIcon icon,
               String desc, Integer mnemonic) {
  			super(label, icon);
  			putValue(SHORT_DESCRIPTION, desc);
  			putValue(MNEMONIC_KEY, mnemonic);
  		}
  	public void actionPerformed(ActionEvent e) {
  		if (e.getActionCommand().equals("Save")) {
  			System.out.println("Save action  "+ e);
  		}
  		else if (e.getActionCommand().equals("Open")) {
  			System.out.println("Open action" + e);
  		}
  		else {
  			System.out.println("Other action" + e);
  		}
  	}
  }
}

