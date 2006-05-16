package phenote.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.Document;
import javax.swing.plaf.basic.BasicComboBoxEditor;
//import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.metal.MetalComboBoxUI;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharField;
import phenote.datamodel.CharField.CharFieldEnum;
import phenote.datamodel.CharacterI;
import phenote.datamodel.Ontology;
import phenote.datamodel.SearchParamsI;
import phenote.edit.EditManager;
import phenote.edit.UpdateTransaction;
import phenote.gui.selection.SelectionManager;

class AutoComboBox extends JComboBox {

  //private String ontology; // --> enum!
  private Ontology ontology;
  private boolean changingCompletionList = false;
  private boolean keyTyped = false;
  private String previousInput = "";
  private boolean doCompletion = true;
  private DefaultComboBoxModel defaultComboBoxModel;
  private SearchParamsI searchParams;
  private boolean inTestMode = false;
  //private AutoTextFieldEditor autoTextFieldEditor;
  private AutoTextField autoTextField;
  private CharField charField;

  AutoComboBox(Ontology ontology,SearchParamsI sp) {
    // this inner class enables retrieving of JList for mouse over
    // this will probably throw errors if non metal look & feel is used
    setUI(new MetalListComboUI());

    this.ontology = ontology;
    searchParams = sp; // singleton access? part of ontology?
    setEditable(true);
    AutoTextFieldEditor autoTextFieldEditor = new AutoTextFieldEditor();
    this.setEditor(autoTextFieldEditor);
    setPreferredSize(new Dimension(325,22));

    addCompletionListListener(new CompletionListListener());

    // just for genetic context for now? or everything?
    addActionListener(new ComboBoxActionListener());

    // mac bug workaround where list covers up textfield on < 12 items no scroll
    // from http://www.orbital-computer.de/JComboBox/#usage
    // it does note this may cause class cast excpetions??
    // it does cause exception when down arror is typed... hmmm...
    //setUI(new BasicComboBoxUI()); // now setting metal look & feel for whole app

  }

  //void setSearchParams(SearchParamsI sp) { searchParams = sp; }

  void setCharField(CharField charField) { this.charField = charField; }

  /** Set text in editable text field of j combo (eg from table select) */
  void setText(String text) {
    // do not do term completion on externally set text!
    boolean doCompletion = false;
    setText(text,doCompletion);
  }

  // text from selecting table doesnt do completion, TestPhenote does
  void setText(String text, boolean doCompletion) {
    this.doCompletion = doCompletion;
    this.keyTyped = doCompletion; // key has to be typed for completion
    getEditor().setItem(text);
    this.doCompletion = true; // set back to default
  }

  /** Return text in text field */
  String getText() {
    //return (String)getSelectedItem();
    return getEditor().getItem().toString(); // or editor.getText() ?
  }


  void clear() {
    setText("");
  }

  //private Ontology getOntology() { return ontology; }

  /** Return true if input String matches name of OBOClass in
   * defaultComboBoxModel - rename this?
   */
  boolean isInCompletionList(String input) {
    if (defaultComboBoxModel == null)
      return false;
    if (input == null) {
      return false;
    }
    // this is wrong as it holds OBOClasses not Strings!
    //return defaultComboBoxModel.getIndexOf(input) != -1;
    // have to go through all OBOClasses and extract there names - bummer
    // most likely input is selected one check that first
    OBOClass selectedClass = getSelectedCompListOboClass();
    if (selectedClass != null && input.equals(selectedClass.getName()))
      return true;
    // selected failed(is this possible?) - try everything in the list then...
    for (int i=0; i<defaultComboBoxModel.getSize(); i++) {
      if (input.equals(getCompListOboClass(i).getName()))
        return true;
    }
    return false;
  }
 
  /** This gets obo class selected in completion list - not from text box 
      Returns null if nothing selected - can happen amidst changing selection */
  private OBOClass getSelectedCompListOboClass() {
    Object obj = defaultComboBoxModel.getSelectedItem();
    if (obj == null)
      return null;
    // need to check obj for null if nothing selected!!!
    return oboClassDowncast(obj);
  }
  private OBOClass getCompListOboClass(int index) {
    Object obj = defaultComboBoxModel.getElementAt(index);
    return oboClassDowncast(obj);
  }

  private OBOClass oboClassDowncast(Object obj) {
    if (obj == null)
      return null;
    if ( ! (obj instanceof OBOClass)) {
      System.out.println("Item in completion list not obo class "+obj.getClass());
      return null;
    }
    return (OBOClass)obj;
  }

  /** BasicComboBoxEditor uses JTextField as its editing component but is
   * only available as a protected variable - odd */
  private class AutoTextFieldEditor extends BasicComboBoxEditor {


    private AutoTextFieldEditor() {
      autoTextField = new AutoTextField();
      editor = autoTextField; // protected editor var from BCBE
      addDocumentListener(new AutoDocumentListener());
      getTextField().addKeyListener(new AutoKeyListener());
    }

    // editor is protected JTextField - wacky
    private JTextField getTextField() {
      return editor;
    }

    private Document getDocument() {
      return getTextField().getDocument();
    }
    private void addDocumentListener(DocumentListener dl) {
      getDocument().addDocumentListener(dl);
    }
  }
  

  /** AutoTextField inner class - ignores set text when in
   * changingCompletionList mode - this is the text field for the
   combo box */
  private class AutoTextField extends JTextField {
    
    private AutoTextField() {
      super(25); // width
    }

    /** dont set text if changing completion list, if changing text turn off
        completion, as changing text is coming from outside not user typing
        thus wont get completion on user selection, leaving popup hanging */
    public void setText(String text) {
      if (changingCompletionList)
	return;
      // this makes setText(text,true) turn to false (called from TextPhenote)
      // but this is needed from mouse release on selection set text
      // is called and will cause completion list to come up after sel
      // w/o it
      doCompletion = false; 
      super.setText(text);
      doCompletion = true;
    }
    
    protected void processKeyEvent(KeyEvent e) {
      //boolean fiddle = KeyboardState.shouldProcess(e);
      super.processKeyEvent(e);
    }
  }

  void simulateLKeyStroke() {
    autoTextField.processKeyEvent(new KeyEvent(this,KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_L,'l'));
    autoTextField.processKeyEvent(new KeyEvent(this,KeyEvent.KEY_TYPED,0,0,KeyEvent.VK_UNDEFINED,'l'));
  }
  
  void simulateKeyStroke(int keyCode, char c) {
    KeyEvent k = new KeyEvent(this,KeyEvent.KEY_PRESSED,0,0,keyCode,c);
    autoTextField.processKeyEvent(k);
    k.setKeyCode(KeyEvent.KEY_RELEASED);
    autoTextField.processKeyEvent(k);
    k = new KeyEvent(this,KeyEvent.KEY_TYPED,0,0,KeyEvent.VK_UNDEFINED,c);
    autoTextField.processKeyEvent(k);
  }

  private class AutoKeyListener extends KeyAdapter {
    // keyTyped doesnt seem to catch backspace in 1.5 - but did in 1.4 - odd
//     public void keyTyped(KeyEvent e)  {
//       // return & tab should be ignored, as well as a lot of other things
//       keyTyped = true;
//       // this may be funny but with key type event the text has not yet be set
//       // this catches cases where text changed due select/action - kinda cheap
//       previousInput = getText();
//     }
    public void keyReleased(KeyEvent e) {
      // return & tab should be ignored, as well as a lot of other things
      keyTyped = true;
      // this may be funny but with key type event the text has not yet be set
      // this catches cases where text changed due select/action - kinda cheap
      previousInput = getText();
    }
  }


  /** disable completion for item being selected - otherwise a popup comes up after
      the selection - probably no harm in keeping this but this may be redundant with
      the turning off of doCompletion in inner ATF.setText which is more
      comprehensive */
  public void setSelectedItem(Object item) {
    doCompletion = false;
    super.setSelectedItem(item);
    doCompletion = true;
  }

  /** Populates defaultComboBoxModel with Vector of OBOClasses - OBOClass.toString
      is the name of the term - thats why its possible - at least for moment
      if we want to put syn in brackets that makes this not possible - certainly
      handy */
  private void doCompletion() {
    if (!doCompletion) // flag set if text filled in externally (from table sel)
      return;
    // so AbstractDoc.replace does a remove and then insert, the remove sets
    // text to "" and then keyTyped gets set to false, so the insert doesnt go
    // through, taking out keyTyped, inputChanged may be sufficient
//     if (!keyTyped) // if user hasnt typed anything dont bother
//       return;
    if (!inputChanged()) // if input is actually same no need to recomp
      return;
    keyTyped = false;
    // too soon - text field doesnt have text yet.... hmmmm....
    String input = getText();
    // this is a vector of OBOClasses
    Vector v = getTerms(input);
    // throws IllegalStateException, Attempt to mutate in notification
    // this tries to change text field amidst notification hmmmm.....
    changingCompletionList = true;
    defaultComboBoxModel = new DefaultComboBoxModel(v);
    setModel(defaultComboBoxModel);
    changingCompletionList = false;
    // showPopup can hang during test but not during real run - bizarre
    showPopup(); // only show popup on key events - actually only do comp w key
  }

  /** This is cheesy but theres a hanging bug with showPopup that only happens
      in test mode - dont know why but doesnt actually matter, so setting flag
      to turn off showpopup in test - if hangs in nontest will investigate */
  void setTestMode(boolean inTestMode) {
    this.inTestMode = inTestMode;
  }

  /** This is cheesy but theres a hanging bug with showPopup that only happens
      in test mode - dont know why but doesnt actually matter, so if inTestMode
      then dont showpopup - if hangs in nontest will investigate */
  public void showPopup() {
    if (inTestMode)
      return;
    super.showPopup();
  }


  /** returns true if input changed from previously recorded input */
  private boolean inputChanged() {
    String newInput = getText();
    boolean inputChanged = ! previousInput.equals(newInput);
    if (inputChanged)
      previousInput = newInput;
    return inputChanged;
  }
  
  /** call Ontology to get a Vector of OBOTerms that contain "in"
      in ontology */
  private Vector getTerms(String in) {
    // or CompletionList.getCompletionList(getOntology()) ??
    //CompletionList cl = CompletionList.getCompletionList();
    //return cl.getCompletionTerms(getOntology(),in,searchParams);
    return ontology.getSearchTerms(in,searchParams);
  }

  private class AutoDocumentListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { doCompletion(); }
    public void insertUpdate(DocumentEvent e) { doCompletion(); }
    public void removeUpdate(DocumentEvent e) { doCompletion(); }
  }

  /** This is touchy stuff - so i want to be able to display info about term in 
      TermInfo when user mouses over terms in combo boxes JList. This is not
      explicitly supported by JComboBox. have to dig into its UI to get JList.
      The combo box ui selects items in JList on mouse over, this listener
      will listen for those mouse over selections 
      should this be done with a selection event - or is that
      overkill, i guess the question will anyone besides term info
      ever care about these mouse over selection - if so make generic */
  void addCompletionListListener(ListSelectionListener lsl) {
    if (!canGetUIJList())
      return;
    getUIJList().addListSelectionListener(lsl);
  }

  private class CompletionListListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      Object source = e.getSource();
      // hate to cast but it is handy here... and it is in fact a JList
      if (!(source instanceof JList)) {
        System.out.println("source of combo box mouse over event is not JList "+
                           source.getClass());
        return;
      }
      JList jList = (JList)source;
      Object selectedValue = jList.getSelectedValue();
      if (selectedValue == null)
        return;
      //System.out.println("sel val "+selectedValue.getClass()+" name "+selectedValue);
      // the selected item should be an OBOClass
      if (!(selectedValue instanceof OBOClass)) {
        System.out.println("selected completion term is not obo class "
                           +selectedValue.getClass());
        return;
      }
      OBOClass oboClass = (OBOClass)selectedValue;
      SelectionManager.inst().selectTerm(AutoComboBox.this,oboClass);
      //setTextFromOboClass(oboClass);
    }
  } // end of CompletionListListener inner class
  
  private SelectionManager getSelectionManager() {
    return SelectionManager.inst();
  }

  private CharacterI getSelectedCharacter() {
    return getSelectionManager().getSelectedCharacter();
  }

  private boolean canGetUIJList() {
    return getUIJList() != null;
  }

  /** This may return null if not using Metal/Java UI/Look & Feel as JList comes
      from the UI - need to implement getting JList from each UI I guess */
  private JList getUIJList() {
    ComboBoxUI comboBoxUI = getUI();
    if (!(comboBoxUI instanceof MetalListComboUI)) {
      System.out.println("Cant retrieve JList for look & feel, cant do mouse overs "
                         +comboBoxUI.getClass());
      return null;
    }
    return ((MetalListComboUI)comboBoxUI).getJList();
  }

  // for TestPhenote
  void doMouseOver(int itemNumber) {
    //System.out.println("AutoComboBox.doMouseOver not implemented yet");
    if (!canGetUIJList()) return;
    JList jList = getUIJList();
    jList.setSelectedIndex(itemNumber);
  }

  private class MetalListComboUI extends MetalComboBoxUI {
    private JList getJList() {
      return listBox; // protected JList in BasicComboBoxUI
    }
  }
  
  

   /** Listens for actions from combo boxes and puts terms into table 
   * actions come from mouse select of term as well as return & tab 
   change this - should only modify character - could be done in ACB except
   gt text field which isnt done here anyways - then send out CharacterChangeEvent
  for table to get and refresh itself */
  private class ComboBoxActionListener implements ActionListener {
    //private String previousModelValue=null;
    private OBOClass previousOboClass=null;

    private ComboBoxActionListener() {//String ontology,AutoComboBox cb) {
      //this.ontology = ontology;
      //comboBox = cb;
    }
    public void actionPerformed(ActionEvent e) {
      editCharField();
      //setTableFromField(ontology);
    }

    /** edits Character field, sends out CharChangeEvent. 
        checks that text in text field from user
        is actually an item in completion list, is an obo term. */
    private void editCharField() {
      String input = getText();
      if (input == null) return; // probably doesnt happen
      // the input should be from selected obo class shouldnt it? is it possible
      // for this not to be so?
      OBOClass oboClass = getSelectedCompListOboClass();
      if (oboClass == null) return; /// happens on return on invalid term name

      // can this happen? yes when user hits return on text - actually i think this
      // is the test for being in the completion list isnt it?
      boolean DEBUG = true;
      if (!input.equals(oboClass.getName())) {
        if (DEBUG)
          System.out.println("User input ["+input+"] and list selection dont match "+
                           "selection: "+oboClass.getName());
        return;
      }
      
      // check if input is a real term - i think we can get away with checking
      // if in present term completion list - not sure
      // i think this is replaced by check above - make sure does the same...
//       boolean valid = isInCompletionList(oboClass); //input);
//       if (!valid)
//         return;
      
      if (charField == null) // shouldnt happen
        return;
      

      CharacterI c = getSelectedCharacter();
      CharFieldEnum cfe = charField.getCharFieldEnum();
      UpdateTransaction ut = new UpdateTransaction(c,cfe,oboClass,previousOboClass);
      //t.editModel();  // or charField.editModel?
      // CharacterChangeEvent e = new CharacterChangeEvent(t);
      // OR CharEditManager.inst().updateModel(c,cfe,input,previousModelValue);
      // CEM.handleTransaction(new UT), CEM.updateModel(UT)
      EditManager.inst().updateModel(this,ut);
      // fireChangeEvent(e);

      // for undo 
      previousOboClass = oboClass;

    }
  }

}

//     /** Sets table value from field. checks that text in text field from user
//         is actually an item in completion list, is an obo term. */
//     private void setTableFromField(String ontology) {
//       String input = comboBox.getText();//getInput(ontology);
      
//       // check if input is a real term - i think we can get away with checking
//       // if in present term completion list - not sure
//       boolean valid = comboBox.isInCompletionList(input);
//       if (!valid)
//         return;
//       // its valid - set the field
//       // no no no - edit model, send out model changed event
//       setTableColumn(ontology,input);
//     }
//   }
 


// doesnt work - would need to subclass editor component i thing - hassle
//   // for TestPhenote
//   void simulateBackspace() {
//     KeyEvent ke = new KeyEvent(this,KeyEvent.VK_DELETE,java.util.Calendar.getInstance().getTimeInMillis(),0,KeyEvent.VK_UNDEFINED,KeyEvent.CHAR_UNDEFINED);
//     //patoComboBox.processKeyEvent(ke);
//     getEditor().getEditorComponent().processKeyEvent(ke);

//   }
//     Document d = e.getDocument();
//     String input;
//     try {
//       input = d.getText(0,d.getLength());
//     }
//     catch (javax.swing.text.BadLocationException ex) {
//       System.out.println(ex);
//       return;
//     }
