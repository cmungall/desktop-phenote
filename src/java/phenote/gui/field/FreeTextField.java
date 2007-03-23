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
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

import org.apache.log4j.Logger;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;
//import phenote.datamodel.CharFieldEnum; // phase out
import phenote.edit.CompoundTransaction;
import phenote.edit.EditManager;
import phenote.gui.selection.SelectionManager;

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
    textField.setMinimumSize(CharFieldGui.inputSize);
    //textField.setPreferredSize(CharFieldGui.inputSize);
    textField.setEditable(true);
    textField.getDocument().addDocumentListener(new TextFieldDocumentListener());
    textField.addFocusListener(new FreeFocusListener());
    textField.addKeyListener(new TextKeyListener());
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
    if (!chr.hasValue(getCharField()))
      return;
    String v = chr.getValue(getCharField()).getName();
    setText(v);
  }
  
  protected void setGuiForMultiSelect() {
    setUpdateGuiOnly(true);//updateGuiOnly = true;
    setText("*"); 
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

  /** if the focus has changed and the gui has been edited then edit the model */
  private class FreeFocusListener implements FocusListener {
    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) {
      updateModel();
    }
  }

  private void updateModel() {
    // if only updating gui (multi select clear) then dont update model
    if (updateGuiOnly()) return;
    if (!guiTextHasChanged) return; // gui hasnt been edited
    List<CharacterI> chars = getSelectedChars();
    String v = getText();
    //CompoundTransaction ct = new CompoundTransaction(chars,getCharFieldEnum(),v);
    CompoundTransaction ct = CompoundTransaction.makeUpdate(chars,getCharField(),v);
    EditManager.inst().updateModel(this,ct);//charFieldGui,ct); // cfg source
    guiTextHasChanged = false; // reset flag
  }
  
  private List<CharacterI> getSelectedChars() {
    return SelectionManager.inst().getSelectedChars();
  }
  
  //private CharFieldEnum getCharFieldEnum() { return charFieldGui.getCharFieldEnum(); }
  
  


  /** This is where it is noted that the gui has been edited, only update  
    the model on focus change if the gui has been actually edited */
  private class TextFieldDocumentListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { setGuiChanged(); }
    public void insertUpdate(DocumentEvent e) { setGuiChanged(); }
    public void removeUpdate(DocumentEvent e) { setGuiChanged(); }
    private void setGuiChanged() { guiTextHasChanged = true; }
  }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
