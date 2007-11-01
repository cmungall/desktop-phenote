package phenote.gui;


import phenote.config.Config;
import phenote.config.ConfigException;
import phenote.config.ConfigFileQueryGui;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.TermNotFoundException;
import phenote.gui.TermInfo2;
import phenote.main.Phenote;
//import phenote.gui.ActionManager;
//import phenote.gui.TermInfo.BackNaviActionListener;
//import phenote.gui.TermInfo.FavoritesNaviActionListener;
//import phenote.gui.TermInfo.ForwardNaviActionListener;
//import phenote.gui.TermInfo.TermComboBoxActionListener;
//import phenote.gui.TermInfo.UseTermActionListener;
//import phenote.gui.actions.OpenFileAction;
//import phenote.gui.actions.SaveAsFileAction;
//import phenote.gui.actions.DuplicateAnnotationAction;
//import phenote.gui.actions.UndoAction;
//import phenote.gui.actions.RedoAction;
//import phenote.gui.actions.CopyAction;
import phenote.gui.actions.ForwardAction;
import phenote.gui.actions.BackAction;

import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.UseTermEvent;
import phenote.gui.selection.UseTermListener;

import java.io.IOException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import javax.swing.*;
import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;

import org.geneontology.oboedit.datamodel.OBOClass;

import java.util.Vector;
import java.awt.*;
import java.awt.event.*;


/**
 * @author Nicole Washington
 * 
 * This is the toolbar to be displayed along with the {@link TermInfo2} panel.
 * The termName element gets informed by what is currently displayed in the
 * TermInfo panel.
 * 
 *
 */
public class TermInfoToolbar extends JToolBar {

  private Config config = Config.inst();
  public static int BUTTON_HEIGHT = 30;
  private static int TERM_INFO_DEFAULT_WIDTH=350;
  private UseTermListener useTermListener;
  private SelectionManager selectionManager;
  private OBOClass currentOboClass = null;




  // Vector for holding all the actions.
  private Vector actions;
  private Vector buttons;
  private JTextArea termField;
  
  public void hideToolbar() {
  	setVisible(false);
  }
  
  public void showToolbar() {
  	setVisible(true);
  }

  public void setTermFieldText(OBOClass oboClass) {
  	termField.setText(oboClass.getName());
  	currentOboClass = oboClass;
  }
  
  public TermInfoToolbar() {
    super("TermInfoToolbar");
    init();
  }

  private void init() {

    buttons = new Vector();

    //Standard things to do for browser
    //The actions ought to be created elsewhere, yeah?
    Action forwardAction = new ForwardAction();
    Action backAction = new BackAction();
//    Action useTermAction = new UseTermAction();    
    
    
    JButton favoritesButton = new JButton();
    JButton backButton = new JButton(backAction);
    JButton forwardButton = new JButton(forwardAction);
    JButton useTermButton = new JButton();

    /*****************************************/
    favoritesButton.setIcon(new ImageIcon("images/Bookmarks24.gif"));
    favoritesButton.setToolTipText("Favorites");

   
    useTermButton.setIcon(new ImageIcon("images/OK.GIF"));
    useTermButton.addActionListener(new UseTermActionListener());
    useTermButton.setToolTipText("Use Term");
   
    termField =  new JTextArea();
    termField.setFont(new Font("Arial", Font.BOLD, 14));
//    termField.setContentType("text/html");
    termField.setText("(no term selected)");
    termField.setPreferredSize(new Dimension((TERM_INFO_DEFAULT_WIDTH-(buttons.size()*BUTTON_HEIGHT)),BUTTON_HEIGHT));
    termField.setEditable(false);  

    buttons.add(backButton);
    buttons.add(forwardButton);
    buttons.add(favoritesButton);  
    buttons.add(useTermButton);
    
    add(termField);
    addSeparator();

    JButton tempButton = null;
    for (int i=0; i<buttons.size(); i++) {
    	tempButton = (JButton)buttons.elementAt(i);
    	if (tempButton.getIcon() != null) {
    		tempButton.setText(""); //an icon-only button
    		tempButton.setPreferredSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
        tempButton.setMinimumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
        tempButton.setMaximumSize(new Dimension(BUTTON_HEIGHT, BUTTON_HEIGHT));
    	}
    		add(tempButton);
    }
    	
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
    setRollover(true);
    showToolbar();
  }
  
  private class UseTermActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      //commitTerm();
      // relation comp list sets to null
      if (useTermListener == null) return;
      if (currentOboClass == null) return; // shouldnt happen
      useTermListener.useTerm(new UseTermEvent(TermInfoToolbar.this,currentOboClass));
    }
  }

}


