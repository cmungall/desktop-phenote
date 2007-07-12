package phenote.gui.field;

import java.util.List;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.Point;




import javax.swing.text.JTextComponent;
import javax.swing.JTextField;
import javax.swing.text.Keymap;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.apache.log4j.Logger;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;
//import phenote.datamodel.CharFieldEnum; // phase out
import phenote.edit.CompoundTransaction;
import phenote.edit.EditManager;
import phenote.gui.selection.CharSelectionEvent;
import phenote.gui.selection.SelectionManager;
import phenote.gui.FieldRightClickMenu;


// should this be a subclass of charfieldGui? maybe?
class FreeTextField extends CharFieldGui {

  private JTextField textField;
  //private CharFieldGui charFieldGui;
  private boolean guiTextHasChanged = false;
  //private boolean updateGuiOnly = false;

  //private void initTextField(String label) {
  FreeTextField(CharField charField) { //CharFieldGui cfg) {
    super(charField);
    //charFieldGui = cfg;
    textField = new JTextField(35);
    //textField.setKeymap(phenote.main.Phenote.defaultKeymap); didnt work
    textField.setMinimumSize(CharFieldGui.inputSize);
    //textField.setPreferredSize(CharFieldGui.inputSize);
    textField.setEditable(true);
    textField.getDocument().addDocumentListener(new TextFieldDocumentListener());
    textField.addFocusListener(new FreeFocusListener());
     //Add listener to components that can bring up popup menus.
    JPopupMenu popup = new FieldRightClickMenu();
    MouseListener popupListener = new PopupListener(popup);
    textField.addMouseListener(popupListener);

    //textField.addKeyListener(new TextKeyListener());
    loadKeyMap(); // mac cut copy paste
  }

  private static final int shortcut = 
    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

  JTextComponent.KeyBinding[] defaultBindings = {
     new JTextComponent.KeyBinding(
       KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcut),
       DefaultEditorKit.copyAction),
     new JTextComponent.KeyBinding(
       KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcut),
       DefaultEditorKit.pasteAction),
     new JTextComponent.KeyBinding(
       KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcut),
       DefaultEditorKit.cutAction),
   };

 
  /** actually i think this only needs to happen once per session - this reinstates
      apples apple c,v,x that gets lost in setting look & feel to metal to 
      alleviate jcombo apple bug */
  private void loadKeyMap() {
    Keymap k = textField.getKeymap();
    JTextComponent.loadKeymap(k, defaultBindings, textField.getActions());
  }

  //JTextField getComponent() { return textField; }
  protected Component getUserInputGui() { return textField; }

  protected void setText(String text) {
    textField.setText(text);
  }
  protected String getText() { return textField.getText(); }

  //private boolean updateGuiOnly() { return /*charFieldGui.*/updateGuiOnly(); }

  protected void setValueFromChar(CharacterI chr) {
    if (chr == null) {
      log().error("ERROR: attempt to set fields from null character"); // ex?
      return;
    }
    //String v = charField.getCharFieldEnum().getValue(chr).getName();
    //if (!chr.hasValue(getCharField())) return;
    if (!chr.hasValue(getCharField())) {
      setText(""); // ?? null?
      return;
    }
    String v = chr.getValue(getCharField()).getName();
    setText(v);
  }
  
  protected void setGuiForMultiSelect() {
    setUpdateGuiOnly(true);//updateGuiOnly = true;
    setText("*"); // this sets gui text changed to true, but probably shouldnt
    // guiTextHasChanged = false; // ???
    setUpdateGuiOnly(false);//updateGuiOnly = false;
  }

  // subclass?
  //private CharField getCharField() { return charFieldGui.getCharField(); }

  /** key listener for free text fields for Cmd-V pasting for macs */
  private class TextKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
      // on a mac Command-V is paste. this aint so with java/metal look&feel
      if (e.getKeyChar() == 'v' 
          && e.getKeyModifiersText(e.getModifiers()).equals("Command")) {
        //log().debug("got cmd V paste");
        //System.getClipboard
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
          Transferable t = c.getContents(null); // null?
          Object s = t.getTransferData(DataFlavor.stringFlavor);
          // this isnt quite right as it should just insert the text not wipe
          // it out - but probably sufficient for now?
          if (s != null)
            setText(s.toString());
        } catch (Exception ex) { System.out.println("failed paste "+ex); }
      }
    }
  }
  
  private class PopupListener extends MouseAdapter {
  	JPopupMenu popup;
  	
  	int col; int row;
  	Point p;
  	PopupListener(JPopupMenu popupMenu) {
  		popup = popupMenu;
  	}

  	public void mousePressed(MouseEvent e) {
//  		super.mousePressed(e);
  		maybeShowPopup(e);
  	}

  	public void mouseReleased(MouseEvent e) {
//  		super.mouseReleased(e);
  		maybeShowPopup(e);
  	}

  	private void maybeShowPopup(MouseEvent e) {
//  		String m="";
//  		col=e.getX();
//  		row=e.getY();
//       System.out.println("col="+col+" row= "+row);
//  		System.out.println("button="+e.getButton());
//  		System.out.println(e.paramString());
//  		System.out.println("popuptrigger="+e.isPopupTrigger());
//  		if(e.getButton()==MouseEvent.BUTTON3) {
  		if (e.isPopupTrigger()) {
//    		m = "popuptrigger!";
    		popup.show(e.getComponent(),
  					e.getX(), e.getY());
  		}
//  		else {
////  			m="no trigger, its "+e.paramString()+"!";
//  		}
  	}
  }

  /** if the focus has changed and the gui has been edited then edit the model */
  private class FreeFocusListener implements FocusListener {
    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) {
      //updateModel();
      FreeTextField.this.focusLost();
    }
  }

  protected void focusLost() { updateModel(); }

  /** update model using currently selected chars */
  private void updateModel() {
    List<CharacterI> chars = getSelectedChars();
    updateModel(chars);
  }

  private void updateModel(List<CharacterI> chars) {
    // if only updating gui (multi select clear) then dont update model
    if (updateGuiOnly()) return;
    if (!guiTextHasChanged) return; // gui hasnt been edited

    String v = getText();
    //CompoundTransaction ct = new CompoundTransaction(chars,getCharFieldEnum(),v);
    CompoundTransaction ct = CompoundTransaction.makeUpdate(chars,getCharField(),v);
    this.getEditManager().updateModel(this,ct);//charFieldGui,ct); // cfg source
    guiTextHasChanged = false; // reset flag

  }

  /** selection (from table) comes in before focus lost! update model with
      previous selection */
  protected void charactersSelected(CharSelectionEvent e) {
    if (e.hasPreviouslySelectedChars())
      updateModel(e.getPreviouslySelectedChars());
    super.charactersSelected(e); // CharFieldGui
  }
  
  private List<CharacterI> getSelectedChars() {
    return this.getSelectionManager().getSelectedChars();
  }
  

  /** This is where it is noted that the gui has been edited, only update  
    the model on focus change if the gui has been actually edited */
  private class TextFieldDocumentListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { setGuiChanged(); }
    public void insertUpdate(DocumentEvent e) { setGuiChanged(); }
    public void removeUpdate(DocumentEvent e) { setGuiChanged(); }
    private void setGuiChanged() { 
      // only note text change if not updating just gui?, * for multi select
      // fixes bug for * multi select editing model
      if (!updateGuiOnly())
        guiTextHasChanged = true;
    }
  }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
