package phenote.gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;

import org.obo.datamodel.OBOClass;

import phenote.config.Config;
import phenote.gui.actions.BackAction;
import phenote.gui.actions.ForwardAction;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.UseTermEvent;
import phenote.gui.selection.UseTermListener;


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
  public static final int BUTTON_HEIGHT = 30;
  private static final int TERM_INFO_DEFAULT_WIDTH=350;
  private UseTermListener useTermListener;
  private SelectionManager selectionManager;
  private OBOClass currentOboClass = null;
  private final int BACKBUTTONINDEX = 0;
  private final int FORWARDBUTTONINDEX = 1;
  private final int FAVBUTTONINDEX = 2;
  private final int USETERMBUTTONINDEX = 3;
  private final int GETANNOTATIONSBUTTONINDEX = 4;




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
    
    JButton getAnnotationsButton = new JButton();
    getAnnotationsButton.setIcon(new ImageIcon("images/somefile.GIF"));
    getAnnotationsButton.setToolTipText("Fetch annoations to this term from OBD");
    getAnnotationsButton.addActionListener(new GetAnnotationsActionListener());

    buttons.add(BACKBUTTONINDEX, backButton);
    buttons.add(FORWARDBUTTONINDEX, forwardButton);
    buttons.add(FAVBUTTONINDEX, favoritesButton);  
    buttons.add(USETERMBUTTONINDEX, useTermButton);
    buttons.add(GETANNOTATIONSBUTTONINDEX, getAnnotationsButton);

    termField =  new JTextArea();
    termField.setFont(new Font("Arial", Font.BOLD, 12));
    termField.setWrapStyleWord(true);
    termField.setLineWrap(true);
//    termField.setContentType("text/html");
    termField.setText("(no term selected)");
    termField.setPreferredSize(new Dimension((TERM_INFO_DEFAULT_WIDTH-(buttons.size()*BUTTON_HEIGHT)),BUTTON_HEIGHT));
//    termField.setMaximumSize(new Dimension((TERM_INFO_DEFAULT_WIDTH-(buttons.size()*BUTTON_HEIGHT)),BUTTON_HEIGHT));
    termField.setEditable(false);  

    add(termField,0);
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
  
  public void setButtonStatus(String button, boolean enabled) {
  	if (button.equals("forward")) {
  		((JButton)buttons.get(FORWARDBUTTONINDEX)).setEnabled(enabled);
  	} else if (button.equals("back")) {
  		((JButton)buttons.get(BACKBUTTONINDEX)).setEnabled(enabled);
  	} else if (button.equals("annotations")) {
  		((JButton)buttons.get(GETANNOTATIONSBUTTONINDEX)).setEnabled(enabled);
  	}
  		
  }
  public void setNaviButtonStatus() {
  	int naviIndex = TermInfo2.inst().getNaviIndex();
  	int tot = TermInfo2.inst().getTermInfoNaviHistory().size();
  	((JButton)buttons.get(FORWARDBUTTONINDEX)).setEnabled(naviIndex < (tot-1));
  	((JButton)buttons.get(BACKBUTTONINDEX)).setEnabled(naviIndex>0);

  }
  
  private class GetAnnotationsActionListener implements ActionListener {
  	public void actionPerformed(ActionEvent e) {
  		TermInfo2.inst().getCurrentAnnotations();
  	}
  }
  
}


