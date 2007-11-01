package phenote.gui;


import java.awt.Color;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import phenote.config.Config;
import phenote.gui.actions.DuplicateAnnotationAction;
import phenote.gui.actions.OpenFileAction;
import phenote.gui.actions.SaveAsFileAction;
import phenote.gui.actions.UndoAction;
import phenote.gui.actions.RedoAction;
import phenote.gui.actions.CopyAction;

// This is the basic toolbar to be displayed, containing items
// such as New, Save, Print, etc.  Could be turned on/off if desired.
// Basically provides the same functionality as in the menus, 
// but doesn't require accessing the menus.


public class StandardToolbar extends JToolBar {

  private Config config = Config.inst();

  // Vector for holding all the actions.
  private Vector actions;
  private Vector buttons;
  
  public void hideToolbar() {
  	setVisible(false);
  }
  
  public void showToolbar() {
  	setVisible(true);
  }

  public StandardToolbar() {
    super("StandardToolbar");
    init();
  }

  private void init() {

    buttons = new Vector();

    //Standard things to do for files
    //The actions ought to be created elsewhere, yeah?
    Action saveAction = new SaveAsFileAction();
    Action openAction = new OpenFileAction();
    Action dupAnnotationAction = new DuplicateAnnotationAction();
    Action undoAction = new UndoAction();
    Action redoAction = new RedoAction();
    Action copyAction = new CopyAction();
    
    
    JButton saveButton = new JButton(saveAction);
    JButton openButton = new JButton(openAction);
    JButton newTermButton = new JButton("New Term", null);
    buttons.add(openButton);
    buttons.add(saveButton);
//buttons.add(newTermButton);

    
    //Standard things for annotations
    //probably ought to be its own toolbar
    JButton newAnnotButton = new JButton("New Annot");
    JButton dupAnnotButton = new JButton(dupAnnotationAction);
    JButton delAnnotButton = new JButton("Del Annot");
    
    JButton undoButton = new JButton(undoAction);
    JButton redoButton = new JButton(redoAction);
    JButton copyButton = new JButton(copyAction);
    buttons.add(undoButton);
    buttons.add(redoButton);
    buttons.add(copyButton);
    
    
//    add(newAnnotButton);
//    add(dupAnnotButton);
    buttons.add(dupAnnotButton);
    JButton tempButton = null;
    for (int i=0; i<buttons.size(); i++) {
    	tempButton = (JButton)buttons.elementAt(i);
    	if (tempButton.getIcon() != null) {
    		tempButton.setText(""); //an icon-only button
    	}
    		add(tempButton);
    }
    addSeparator();
    	
    setBackground(Color.GRAY);
    setBorder(new BevelBorder(BevelBorder.LOWERED));
    setRollover(true);
    hideToolbar();
  }
}

//ActionManager am = new ActionManager();
//Action newAction = am.inst().getAction("New");
//JButton newButton = new JButton(newAction);
