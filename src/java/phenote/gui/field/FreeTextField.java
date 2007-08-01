package phenote.gui.field;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
//import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import phenote.edit.CompoundTransaction;
import phenote.gui.ErrorEvent;
import phenote.gui.ErrorManager;
import phenote.gui.FieldRightClickMenu;
import phenote.gui.GuiUtil;
import phenote.gui.selection.CharSelectionEvent;
import phenote.main.Phenote;


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
    textField = new JTextField();
    //textField.setKeymap(phenote.main.Phenote.defaultKeymap); didnt work
    textField.setEditable(true);
    textField.getDocument().addDocumentListener(new TextFieldDocumentListener());
    textField.addFocusListener(new FreeFocusListener());
     //Add listener to components that can bring up popup menus.
    JPopupMenu popup = new FieldRightClickMenu();
    MouseListener popupListener = new PopupListener(popup);
    textField.addMouseListener(popupListener);

    if (hasInputVerifier())
      textField.setInputVerifier(getInputVerifier());

    //textField.addKeyListener(new TextKeyListener());
    loadKeyMap(); // mac cut copy paste
  }

  protected boolean hasInputVerifier() {
    return getInputVerifier() != null;
  }

  /** overridden by subclasses that verify input (IdFieldGui, IntFieldGui...) */
  protected InputVerifier getInputVerifier() { return null; }

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
  
  // whats this about???
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
    this.getEditManager().updateModel(this,ct);//charFieldGui,ct); // cfg source
    guiTextHasChanged = false; // reset flag

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

  /** selection (from selMan/table) comes in before focus lost! update model with
      previous selection */
  protected void charactersSelected(CharSelectionEvent e) {
    //new Throwable().printStackTrace();
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
  protected Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
