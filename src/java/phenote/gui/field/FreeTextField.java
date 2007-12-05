package phenote.gui.field;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.edit.CompoundTransaction;
import phenote.error.ErrorEvent;
import phenote.error.ErrorManager;
import phenote.gui.FieldRightClickMenu;
import phenote.gui.GuiUtil;
import phenote.gui.PopupListener;

class FreeTextField extends CharFieldGui {

  private JTextField textField;
  //private CharFieldGui charFieldGui;
  private boolean guiTextHasChanged = false;
  private boolean selectionChangedBeforeLosingFocus = false;
  private List<CharacterI> editedCharacters = new ArrayList<CharacterI>();
  //private boolean updateGuiOnly = false;

  //private void initTextField(String label) {
  FreeTextField(CharField charField) { //CharFieldGui cfg) {
    super(charField);
    this.getTextField().setEditable(true);
    this.getTextField().addActionListener(new FieldActionListener());
    this.getTextField().getDocument().addDocumentListener(new TextFieldDocumentListener());
     //Add listener to components that can bring up popup menus.
    JPopupMenu popup = new FieldRightClickMenu(this.textField);
    MouseListener popupListener = new PopupListener(popup);
    this.getTextField().addMouseListener(popupListener);

    if (hasInputVerifier())
      this.getTextField().setInputVerifier(getInputVerifier());
  }

  protected boolean hasInputVerifier() {
    return getInputVerifier() != null;
  }

  /** overridden by subclasses that verify input (IdFieldGui, IntFieldGui...) */
  protected InputVerifier getInputVerifier() { return null; }

  protected JComponent getUserInputGui() {
    return this.getTextField();
    }
  
  private JTextField getTextField() {
    if (this.textField == null) {
      this.textField = new JTextField();
    }
    return this.textField;
  }

  protected void setText(String text) {
    this.setUpdateGuiOnly(true);
    textField.setText(text);
    this.setUpdateGuiOnly(false);
  }
  protected String getText() { return textField.getText(); }

  //private boolean updateGuiOnly() { return /*charFieldGui.*/updateGuiOnly(); }

  protected void setValueFromChar(CharacterI chr) {
    if (chr == null) {
      log().error("Attempt to set fields from null character"); // ex?
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
  
  protected void setCharFieldValue(CharFieldValue value) {
    this.setText(value.getName());
  }
  
  protected void focusLost() {
    super.focusLost();
    if (!this.shouldResetGuiForMultipleValues()) {
      this.updateModel();
    }
    this.selectionChangedBeforeLosingFocus = false;
  }
  
  protected boolean hasFocus() {
    return this.getTextField().hasFocus();
  }
  
  @Override
  public void valueChanged(ListSelectionEvent e) {
    this.selectionChangedBeforeLosingFocus = true;
    super.valueChanged(e);
  }
  
  @Override
  protected void setValueFromChars(List<CharacterI> characters) {
    if (this.selectionChangedBeforeLosingFocus) {
      final boolean previousUpdateValue = this.updateGuiOnly();
      this.setUpdateGuiOnly(false);
      this.updateModel(this.editedCharacters);
      this.setUpdateGuiOnly(previousUpdateValue);
    }
    this.editedCharacters = new ArrayList<CharacterI>(characters);
    super.setValueFromChars(characters);
  }

  // subclass?
  //private CharField getCharField() { return charFieldGui.getCharField(); }
  
  private class FieldActionListener implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      FreeTextField.this.updateModel();
    }
    
  }

  // whats this about???
//  private class PopupListener extends MouseAdapter {
//  	JPopupMenu popup;
//  	
//  	int col; int row;
//  	Point p;
//  	PopupListener(JPopupMenu popupMenu) {
//  		popup = popupMenu;
//  	}
//
//  	public void mousePressed(MouseEvent e) {
////  		super.mousePressed(e);
//  		maybeShowPopup(e);
//  	}
//
//  	public void mouseReleased(MouseEvent e) {
////  		super.mouseReleased(e);
//  		maybeShowPopup(e);
//  	}
//
//  	private void maybeShowPopup(MouseEvent e) {
////  		String m="";
////  		col=e.getX();
////  		row=e.getY();
////       System.out.println("col="+col+" row= "+row);
////  		System.out.println("button="+e.getButton());
////  		System.out.println(e.paramString());
////  		System.out.println("popuptrigger="+e.isPopupTrigger());
////  		if(e.getButton()==MouseEvent.BUTTON3) {
//  		if (e.isPopupTrigger()) {
////    		m = "popuptrigger!";
//    		popup.show(e.getComponent(),
//  					e.getX(), e.getY());
//  		}
////  		else {
//////  			m="no trigger, its "+e.paramString()+"!";
////  		}
//  	}
//  }

  /** update model using currently selected chars */
  protected void updateModel() {
    List<CharacterI> chars = getSelectedChars();
    updateModel(chars);
  }

  private void updateModel(List<CharacterI> chars) {
    if (chars.isEmpty()) return;
    // if only updating gui (multi select clear) then dont update model
    if (updateGuiOnly()) return;
    if (!guiTextHasChanged) return; // gui hasnt been edited
    if (!this.hasChangedMultipleValues()) return;
    if (!verify()) {
      //JOptionPane.showMessageDialog(null,getConstraintFailureMsg(),"Input Error",
      //                                JOptionPane.ERROR_MESSAGE); 
//          try { SwingUtilities.invokeLater(new ErrMsgThread()); } // invokeAndWait
//          catch (Exception e) { log().error("err msg thread ex "+e); }
      //fireErrorEvent(getConstraintFailureMsg());
      return; // error message?
    }
    String v = getText();

    // if constraints fail - ie IDField doesnt have : - then passesConstraints is
    // responsible for putting up error message?
    // InputVerifier should take care of this?
//     if (!passesConstraints(v)) {
//       displayConstraintFailureMsg(); //??
//       guiTextHasChanged = false; // ?????
//       textField.requestFocus(false); // false - not temporary
//       // if a new row was selected old row should get selected somehow....
//       return; // failed - dont edit model - clear out field?
//     }

    //CompoundTransaction ct = new CompoundTransaction(chars,getCharFieldEnum(),v);
    CompoundTransaction ct = CompoundTransaction.makeUpdate(chars,getCharField(),v);
    setDoingInternalEdit(true);
    this.getEditManager().updateModel(this,ct);//charFieldGui,ct); // cfg source
    guiTextHasChanged = false; // reset flag
    setDoingInternalEdit(false);

  }

  //private Timer timer;

  protected void fireErrorEvent(String m) {
//     textField.setBackground(java.awt.Color.RED);
//     textField.repaint();
//     Blinker b = new Blinker();
//     Timer timer = new Timer(300,b);
//     b.setTimer(timer);
//     timer.start();
    GuiUtil.doBlinker(textField);
    ErrorManager.inst().error(new ErrorEvent(this,m));
//     textField.repaint();
//     try { Thread.sleep(1000); } catch (InterruptedException e) {}
//     textField.setBackground(java.awt.Color.BLUE);
//     textField.repaint();
//     try { Thread.sleep(1000); } catch (InterruptedException e) {}
//     textField.setBackground(java.awt.Color.RED);
  }

  private class Blinker implements ActionListener {
    private int counter = 0;
    private Timer timer;
    public void actionPerformed(ActionEvent e) {
      Color c = Color.RED;
      if (counter++ % 2 == 0) c = Color.WHITE; 
      textField.setBackground(c);
      if (counter == 5) timer.stop();
    }
    private void setTimer(Timer t) { timer = t; }
  }

//   private class ErrMsgThread implements Runnable {
//     public void run() {
//       JOptionPane.showMessageDialog(null,getConstraintFailureMsg(),"ID Input Error",
//                                     JOptionPane.ERROR_MESSAGE); 
//     }
//   }

  protected boolean verify() {
    if (!hasInputVerifier()) return true;
    return getInputVerifier().verify(textField);
  }

  // no constraints for free text - subclasses override this
  // maybe there should be an abstract method/class?
//   protected boolean passesConstraints(String input) {
//     return true;
//   }

//   private void displayConstraintFailureMsg() {
//     boolean modal = false; //true;
//     final JDialog d = new JDialog(Phenote.getPhenote().getFrame(),"Input Error",modal);
//     // causes it to hang
//     //JOptionPane.showInternalMessageDialog(d,getConstraintFailureMsg(),"Input Error",
//     //                          JOptionPane.ERROR_MESSAGE); 
//     // causes table selection event to not go through(???)
//     //JOptionPane.showMessageDialog(null,getConstraintFailureMsg(),"Input Error",
//     //                            JOptionPane.ERROR_MESSAGE); 
//     d.add(new JLabel(getConstraintFailureMsg()),"North");
//     JButton ok = new JButton("OK");
//     ok.addActionListener(new ActionListener() {
//         public void actionPerformed(ActionEvent e) {
//           System.out.println("disposing "+d);
//           d.dispose();}});
//     d.add(ok,"South");
//     d.setAlwaysOnTop(true);
//     d.pack();
//     d.show();
//   }

  protected String getConstraintFailureMsg() { return null; }

  /** This is where it is noted that the gui has been edited, only update  
    the model on focus change if the gui has been actually edited */
  private class TextFieldDocumentListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { setGuiChanged(); }
    public void insertUpdate(DocumentEvent e) { setGuiChanged(); }
    public void removeUpdate(DocumentEvent e) { setGuiChanged(); }
    private void setGuiChanged() { 
      // only note text change if not updating just gui?, * for multi select
      // fixes bug for * multi select editing model
      if (!updateGuiOnly()) {
        FreeTextField.this.setHasChangedMultipleValues(true);
        guiTextHasChanged = true;
      }
    }
  }

  private Logger log;
  protected Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
