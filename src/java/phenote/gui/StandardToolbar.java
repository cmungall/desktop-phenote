package phenote.gui;


import java.awt.Color;
import java.io.FileNotFoundException;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.bbop.framework.AbstractGUIComponent;

import phenote.config.Config;
import phenote.gui.actions.CopyAction;
import phenote.gui.actions.DbCommitAction;
import phenote.gui.actions.OpenFileAction;
import phenote.gui.actions.RedoAction;
import phenote.gui.actions.SaveFileAction;
import phenote.gui.actions.UndoAction;

/** This is the basic toolbar to be displayed, containing items
// such as New, Save, Print, etc.  Could be turned on/off if desired.
// Basically provides the same functionality as in the menus, 
// but doesn't require accessing the menus.
currently this is used in phenote-plus but not yet in old phenote
only has open & save at the moment */


public class StandardToolbar extends AbstractGUIComponent {

  private Vector<JButton> buttons;
  
  private JToolBar toolbar;
  
  public void hideToolbar() {
  	toolbar.setVisible(false);
  }
  
  public void showToolbar() {
  	toolbar.setVisible(true);
  }

  public StandardToolbar() {
    super("StandardToolbar");
    init();
  }

  
  public void init() {
  	toolbar = new JToolBar();
    buttons = new Vector<JButton>();

    //Standard things to do for files
    //The actions ought to be created elsewhere, yeah?
    Action saveAction = null;
		try {
			saveAction = new SaveFileAction();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    Action openAction=null;
		try {
			openAction = new OpenFileAction();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    Action undoAction = new UndoAction();
    Action redoAction = new RedoAction();
    Action copyAction = new CopyAction();
    
    
    JButton saveButton = new JButton(saveAction);
    JButton openButton = new JButton(openAction);
    buttons.add(openButton);
    buttons.add(saveButton);
    if (Config.inst().hasQueryableDataAdapter()) {
      buttons.add(new JButton(new DbCommitAction()));
    }
    
    JButton undoButton = new JButton(undoAction);
    JButton redoButton = new JButton(redoAction);
    // whats the diff between copy & dup? same right?
    JButton copyButton = new JButton(copyAction);
//    buttons.add(undoButton);
//    buttons.add(redoButton);
//    buttons.add(copyButton);
    
    
//    add(newAnnotButton);
//    add(dupAnnotButton);
//    buttons.add(dupAnnotButton);

    // whats this about???
    JButton tempButton = null;
    for (int i=0; i<buttons.size(); i++) {
    	tempButton = (JButton)buttons.elementAt(i);
    	if (tempButton.getIcon() != null) {
    		tempButton.setText(""); //an icon-only button
    	}
    		toolbar.add(tempButton);
    }
    toolbar.addSeparator();
    	
    toolbar.setBackground(Color.LIGHT_GRAY);
//    toolbar.setBackground(GUIManager.getManager().getFrame().getBackground());
    toolbar.setBorder(new BevelBorder(BevelBorder.LOWERED));
    toolbar.setRollover(true);
    showToolbar();
  }
  
  @Override
  public JToolBar getComponent() {
  	return toolbar;
  }
  
//  @Override
//  public void setBackground(Color color) {
//  	if (color!=null) 
//  		toolbar.setBackground(color);
//  	else
//  		toolbar.setBackground(Color.LIGHT_GRAY);
//  }
  
}

//ActionManager am = new ActionManager();
//Action newAction = am.inst().getAction("New");
//JButton newButton = new JButton(newAction);
