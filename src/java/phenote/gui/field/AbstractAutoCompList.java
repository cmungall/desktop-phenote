package phenote.gui.field;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
//import javax.swing.event.ListSelectionEvent;
//import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.text.Document;
import javax.swing.plaf.basic.BasicComboBoxEditor;
//import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.ComboBoxUI;
import javax.swing.plaf.metal.MetalComboBoxUI;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
//import phenote.datamodel.CharFieldEnum;
//import phenote.datamodel.CharacterI;
//import phenote.datamodel.Ontology;

/** The jcombobox that does auto completion - i had to do some tricks(hacks) to get it
    working with mouse over which doesnt come naturally to jcombobox */

public abstract class AbstractAutoCompList extends JComboBox {

  //private Ontology ontology;
  private boolean changingCompletionList = false;
  //private boolean keyTyped = false;
  //private String previousInput = "";
  private boolean doCompletion = true;
  // should we keep state of currentOboClass which is null if not a valid one?
  // default combo box.getSelectedItem sortof does this imperfectly
  //private OBOClass currentOboClass=null;
  //private OBOProperty currentRel=null;
  private DefaultComboBoxModel defaultComboBoxModel;
  //private SearchParamsI searchParams;
  private boolean inTestMode = false;
  //private AutoTextFieldEditor autoTextFieldEditor;
  private AutoTextField autoTextField;
  private CharField charField;
  ///** Whether differentia of a post composed term */
  //private boolean isDifferentia = false;
  /** if false then model is not edited */
  private boolean editModel;
  //private CompletionListListener compListListener = new CompletionListListener();
  private CompListSearcher compListSearcher;
  private boolean setGuiForMultiSelect = false;

  /** @param editModel if false then ACB doesnt edit model directly (post comp) 
   can abstract classes have constructors - if not init() */
  protected AbstractAutoCompList(CompListSearcher s,boolean editModel,CharField cf) {
    // this inner class enables retrieving of JList for mouse over
    // this will probably throw errors if non metal look & feel is used
    setUI(new MetalListComboUI());
    setEditable(true);
    setPreferredSize(new Dimension(390,20));
    setMaximumSize(new Dimension(390,20));
    // this is super critical - fixes bug where layout goes to hell if string are long
    // in completion - dont ask me why????
    setMinimumSize(new Dimension(390,20));
    AutoTextFieldEditor autoTextFieldEditor = new AutoTextFieldEditor();
    this.setEditor(autoTextFieldEditor);
    // dont know why by setting fonts this seem to get worse not better in terms of
    // the wierd layout issue with large terms & list
    //setFont(new Font("Courier",Font.PLAIN,12)); yuck
    //setFont(new Font("Lucida Console",Font.PLAIN,12)); not fixed
    //Font[] fonts = 
    //java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    //for (Font f : fonts) System.out.println(f);
    //setFont(new Font("Lucida Typewriter",Font.PLAIN,10));
    setFont(new Font("Monospaced",Font.PLAIN,10));
    //setOntology(ontology);
    //searchParams = sp; // singleton access? part of ontology?
    setCharField(cf);
    compListSearcher = s;
    //enableTermInfoListening(true); // default - hardwired in rel & term subclasses
    //addCompletionListListener(compList);
    //if (editModel) // ComboBoxActionListener edits the model
    this.editModel = editModel;
    addActionListener(new ComboBoxActionListener());

  }

  protected CompListSearcher getCompListSearcher() { return compListSearcher; }

  //void setOntology(Ontology o) { ontology = o; }

  //void setSearchParams(SearchParamsI sp) { searchParams = sp; }

  void setCharField(CharField charField) { this.charField = charField; }

  protected CharField getCharField() { return charField; }

  protected boolean hasMoreThanOneOntology() {
    return charField.hasMoreThanOneOntology();
  }

  protected boolean editModelEnabled() { return editModel; }

  /** char in table changed - adjust - not needed for rel(at least not yet)
      as post comp doesnt listen to table changes (does it? should it?), 
      just term */
  protected abstract void setValueFromChar(CharacterI chr);
  // {charField.getCharFieldEnum().getValue(chr).getOboClass() }

  ///** If true than the auto combo is for setting the differentia in a post comp term,
  //    if false (default) than no post comp or genus in post comp */
  //void setIsDifferentia(boolean isDiff) { isDifferentia = isDiff; }

  /** Set text in editable text field of j combo (eg from table select) */
  public void setText(String text) {
    // do not do term completion on externally set text!
    boolean doCompletion = false;
    setText(text,doCompletion);
  }

  /** text from selecting table doesnt do completion, TestPhenote does */
  public void setText(String text, boolean doCompletion) {
    this.doCompletion = doCompletion;
    //this.keyTyped = doCompletion; // key has to be typed for completion
    getEditor().setItem(text);
    //if (charField!=null)log().debug(charField.getName()+" setting text ["+text+"]");
    //new Throwable().printStackTrace();
    this.doCompletion = true; // set back to default
  }

  /** for now just clear out gui without editing model */
  void setGuiForMultiSelect() {
    // flag to prevent AutoTextField.setText from setting to current term
    // is there a cleaner way to do this?? - this falg may need to be generalized
    // as may want for user being able to set to "" (non-required field gen-con)
    setGuiForMultiSelect = true;
    setText("*",false); // false -> no completion, "*"?
    setGuiForMultiSelect = false;
  }

  /** Return text in text field */
  public String getText() {
    //return (String)getSelectedItem();
    return getEditor().getItem().toString(); // or editor.getText() ?
  }


  void clear() {
    setText("");
  }

  protected Object getSelectedObject() throws OboException {
    if (defaultComboBoxModel == null) throw new OboException(); // ??
    Object obj = defaultComboBoxModel.getSelectedItem();
    if (obj == null) throw new OboException();
    return obj;
  }


  /** BasicComboBoxEditor uses JTextField as its editing component but is
   * only available as a protected variable - odd 
   adds auto doc & auto key listeners to combo box edit field 
  I think the purpose of this class is to set its editor var to AutoTextField
  subclass of JTextField */
  private class AutoTextFieldEditor extends BasicComboBoxEditor {

    private AutoTextFieldEditor() {
      autoTextField = new AutoTextField(); // outer instance var for testing
      editor = autoTextField; // protected editor var from BCBE
      addDocumentListener(new AutoDocumentListener());
      //getTextField().addKeyListener(new AutoKeyListener());
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
      //if (charField!=null)
      //log().debug(charField.getName()+" AutoTextField.setText ["+text+"]"+getCurrentTermRelName());
      //new Throwable().printStackTrace();
      doCompletion = false;
      //this is problematic for syns & such where string is diff than term name
      // JComboBox sets this text AFTER got event and set to name
      //super.setText(text); 
      // this works as only time setText is called with cngCL false is with
      // selection - or at least so it seems
      // well that was true but now need to clear text for multi sel
      //System.out.println("ATF.settext ["+text+"] curr term rel name "+getCurrentTermRelName());
      if (setGuiForMultiSelect) // flag will probably need to be more general?
        super.setText(text);
      else
        super.setText(getCurrentTermRelName()); // set to term name with syn select
      doCompletion = true;
    }

    protected void processKeyEvent(KeyEvent e) {
      //boolean fiddle = KeyboardState.shouldProcess(e);
      super.processKeyEvent(e);
    }
  }


  /** Return the name of the current term or relation - was gonna call this
      item name but that gets confused with "items" in combo box */
  protected abstract String getCurrentTermRelName();




  /** disable completion for item being selected - otherwise a popup comes up after
      the selection - probably no harm in keeping this but this may be redundant with
      the turning off of doCompletion in inner ATF.setText which is more
      comprehensive */
  public void setSelectedItem(Object item) {
    doCompletion = false;
    super.setSelectedItem(item);
    doCompletion = true;
  }

  /** If combo box is relationship then the items will be OBOProperties not
      OBOClasses */
  private boolean isRelationshipList() {
    return charField.isRelationship();
  }
  private boolean isTermList() { return !isRelationshipList(); }

  /** MAKE COMPLETION LIST FROM USER INPUT
      Populates defaultComboBoxModel with Vector of OBOClasses - OBOClass.toString
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
    //log().debug("inputchanged "+inputChanged());
    // this seems like a good idea but leads to a bug - if user types a letter
    // then hits "New" then comes back and types same letter then this stops comp
    // also i dont think this actually stops any funny behavior
    //if (!inputChanged()) // if input is actually same no need to recomp
    //return;
    //keyTyped = false;
    // too soon - text field doesnt have text yet.... hmmmm....
    String input = getText();
    // this is a vector of OBOClasses
    // i think ultimately we will need to wrap the OBOClass to be able to
    // have more control over the string - cut off w ... & [syn][obs] tags
    // returns a vector of CompletionTerms (checks if relations)
    Vector v = getSearchItems(input); // abstract method
    //if (isRelationshipList()) v = ontology.getStringMatchRelations(input);
    //else v = getTerms(input);
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
  public void setTestMode(boolean inTestMode) {
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


//   /** returns true if input changed from previously recorded input */
//   private boolean inputChanged() {
//     String newInput = getText();
//     boolean inputChanged = ! previousInput.equals(newInput);
//     if (inputChanged)
//       previousInput = newInput;
//     return inputChanged;
//   }

  /** call Ontology to get a Vector of OBOClass's that contain "in"
      in ontology */
//   private Vector<OBOClass> getTermsOld(String in) {
//     // or CompletionList.getCompletionList(getOntology()) ??
//     //CompletionList cl = CompletionList.getCompletionList();
//     //return cl.getCompletionTermList(getOntology(),in,searchParams);
//     return ontology.getSearchTerms(in,searchParams); // vector of OBOClass's
//   }

  // Vector<CompletionTerm>? CompletionItem?
  protected abstract Vector getSearchItems(String input);
//     if (isRelationshipList()) return termSearcher.getStringMatchRelations(input);
//     else return termSearcher.getStringMatchTerms(input);}

  private class AutoDocumentListener implements DocumentListener {
    public void changedUpdate(DocumentEvent e) { doCompletion(); }
    public void insertUpdate(DocumentEvent e) { doCompletion(); }
    public void removeUpdate(DocumentEvent e) { doCompletion(); }
  }


  protected boolean canGetUIJList() { // move to term comp?
    return getUIJList() != null;
  }

  /** This may return null if not using Metal/Java UI/Look & Feel as JList comes
      from the UI - need to implement getting JList from each UI I guess */
  protected JList getUIJList() {
    ComboBoxUI comboBoxUI = getUI();
    if (!(comboBoxUI instanceof MetalListComboUI)) {
      System.out.println("Cant retrieve JList for look & feel, cant do mouse overs "
                         +comboBoxUI.getClass());
      return null;
    }
    return ((MetalListComboUI)comboBoxUI).getJList();
  }

  // for TestPhenote
  public void doMouseOver(int itemNumber) {
    //System.out.println("AutoComboBox.doMouseOver not implemented yet");
    if (!canGetUIJList()) return;
    JList jList = getUIJList();
    jList.setSelectedIndex(itemNumber);
  }

  private class MetalListComboUI extends MetalComboBoxUI {
    private JList getJList() {
      return listBox; // protected JList in BasicComboBoxUI
    }
    // hmmmmmm - layout issues... actually the fix was to set the minimum size
    // issue was with big terms screwing up layout
//     public void layoutComboBox(java.awt.Container parent, MetalComboBoxLayoutManager manager ) {
//       javax.swing.Icon icon = ((javax.swing.plaf.metal.MetalComboBoxButton)arrowButton).getComboIcon();
//       java.awt.Insets buttonInsets = arrowButton.getInsets();
//       java.awt.Insets insets = comboBox.getInsets();
//       int buttonWidth = icon.getIconWidth() + buttonInsets.left +  buttonInsets.right;  
//       log().debug(" comb width "+comboBox.getWidth()+" inset right "+insets.right+" but width "+ buttonWidth+" in left "+insets.left+" l2r "+ comboBox.getComponentOrientation().isLeftToRight()+rectangleForCurrentValue()+editor.getBounds());
//       super.layoutComboBox(parent,manager);
//     }    
  }



   /** Listens for actions from combo boxes and edits model/character 
   * actions come from mouse select of term as well as return & tab  */
   private class ComboBoxActionListener implements ActionListener {
    //private OBOClass previousOboClass=null;

    private ComboBoxActionListener() {}
    public void actionPerformed(ActionEvent e) {
      listItemSelected();
    }

    /** edits Character field via EditManager if editModel is true. 
        checks that text in text field from user
        is actually an item in completion list, is an obo term. */
    private void listItemSelected() {
      String input = getText();
      if (input == null) return; // probably doesnt happen

//       // the input should be from selected obo class shouldnt it? is it possible
//       // for this not to be so? returns null if no oboclass?
//       // TERM
//       if (isTermList()) {
//         try { currentOboClass = getSelectedOboClass(); }
//         // happens on return on invalid term name
//         catch (OboException e) { return; } // error msg?
//         //if (oboClass == null) return; currentOboClass = oboClass;
//       }
//       // RELATIONSHIP
//       else {
//         try { currentRel = getSelectedRelation(); }
//         catch (OboException e) { return; }
//       }
      // abstract method - sets currentSelItem, returns false if not valid
      try { setCurrentValidItem(); }
      catch (OboException e) { return; } // if not valid then dont edit model

      // EDIT MODEL 
      if (editModel)
        editModel();
    }
  }

  /** The user has selected a real item (from list) record this in the subclass
      in TermCL set currentSelOboClass, in RelCL set currentSelectedRelation 
      throws ex if in fact current user input is not a valid item */
  protected abstract void setCurrentValidItem() throws OboException;

  /** Override - configureEditor is called when user selects item and sets text
      field to item selected, unfortunately this happens after the listening 
      code in this class that sets to term name (not syn name), and then the syn
      name gets set - so this is to catch & repress the subsequent syn name 
      setting - hope that makes sense heres the jdocs from JComboBox for this method:
      Initializes the editor with the specified item. It seems to be ok as far
      as i can tell to supress this method entirely - if this ends up being 
      problematic then this should be coulpled with a flag set in setCurrentValidItem
      or a related method to supress the syn coming after term set */
  public void configureEditor(ComboBoxEditor anEditor,Object anItem) {
    //log().debug("configure editor called"+anItem);
    //new Throwable().printStackTrace();
    // it appears to be ok to supress this entirely
    super.configureEditor(anEditor,anItem); // ??? supress
  }


  protected abstract void editModel();

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
  public void simulateLKeyStroke() {
    autoTextField.processKeyEvent(new KeyEvent(this,KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_L,'l'));
    autoTextField.processKeyEvent(new KeyEvent(this,KeyEvent.KEY_TYPED,0,0,KeyEvent.VK_UNDEFINED,'l'));
  }

  public void simulateKeyStroke(int keyCode, char c) {
    KeyEvent k = new KeyEvent(this,KeyEvent.KEY_PRESSED,0,0,keyCode,c);
    autoTextField.processKeyEvent(k);
    k.setKeyCode(KeyEvent.KEY_RELEASED);
    autoTextField.processKeyEvent(k);
    k = new KeyEvent(this,KeyEvent.KEY_TYPED,0,0,KeyEvent.VK_UNDEFINED,c);
    autoTextField.processKeyEvent(k);
  }

}

class OboException extends Exception {
  OboException() { super(); }
  OboException(String s) { super(s); }
}

// GARBAGE
//   private class AutoKeyListener extends KeyAdapter {
//     // keyTyped doesnt seem to catch backspace in 1.5 - but did in 1.4 - odd
// //     public void keyTyped(KeyEvent e)  {
// //       // return & tab should be ignored, as well as a lot of other things
// //       keyTyped = true;
// //       // this may be funny but with key type event the text has not yet be set
// //       // this catches cases where text changed due select/action - kinda cheap
// //       previousInput = getText();
// //     }
//     public void keyReleased(KeyEvent e) {
//       // return & tab should be ignored, as well as a lot of other things
//       //keyTyped = true;
//       // this may be funny but with key type event the text has not yet be set
//       // this catches cases where text changed due select/action - kinda cheap
//       previousInput = getText();
//     }
//   }
//   private void editModel() {
//     OBOClass oboClass;
//     try { oboClass = getCurrentOboClass(); }
//     catch (Exception e) { return; } // shouldnt happen, error?
//     if (charField == null)  return; // shouldnt happen
//     CharacterI c = getSelectedCharacter(); // from selectionManager
//     CharFieldEnum cfe = charField.getCharFieldEnum();
//     // isDifferentia boolean?
//     UpdateTransaction ut = new UpdateTransaction(c,cfe,oboClass);
//     EditManager.inst().updateModel(this,ut);
//   }
//   /** Listens for UseTermEvents from term info,if editModel is true then edits model*/
//   private class ComboUseTermListener implements UseTermListener {
//     public void useTerm(UseTermEvent e) {
//       setOboClass(e.getTerm());
//       if (editModel) editModel();
//     }
//   }
//   /** This is touchy stuff - so i want to be able to display info about term in 
//       TermInfo when user mouses over terms in combo boxes JList. This is not
//       explicitly supported by JComboBox. have to dig into its UI to get JList.
//       The combo box ui selects items in JList on mouse over, this listener
//       will listen for those mouse over selections 
//       should this be done with a selection event - or is that
//       overkill, i guess the question will anyone besides term info
//       ever care about these mouse over selection - if so make generic */
// //  void addCompletionListListener(ListSelectionListener lsl) {
// //     if (!canGetUIJList()) return;
// //     getUIJList().addListSelectionListener(lsl);  }
//   void enableTermInfoListening(boolean enable) {
//     if (!canGetUIJList())
//       return;
//     if (enable)
//       getUIJList().addListSelectionListener(compListListener);
//     else
//       getUIJList().removeListSelectionListener(compListListener);
//   }

//   /** this is for MOUSE OVER TERM INFO - changes selection */
//   private class CompletionListListener implements ListSelectionListener {
//     public void valueChanged(ListSelectionEvent e) {
//       Object source = e.getSource();
//       // hate to cast but it is handy here... and it is in fact a JList
//       if (!(source instanceof JList)) {
//         System.out.println("source of combo box mouse over event is not JList "+
//                            source.getClass());
//         return;
//       }
//       JList jList = (JList)source;
//       Object selectedValue = jList.getSelectedValue();
//       if (selectedValue == null)
//         return;
//       //System.out.println("sel val "+selectedValue.getClass()+" name "+selectedValue);
//       // the selected item should be an OBOClass
//       if (!(selectedValue instanceof OBOClass)) {
//         System.out.println("selected completion term is not obo class "
//                            +selectedValue.getClass());
//         return;
//       }
//       OBOClass oboClass = (OBOClass)selectedValue;
//       Object src = AbstractAutoCompList.this;
//       getSelectionManager().selectTerm(src,oboClass,getUseTermListener());
//       //setTextFromOboClass(oboClass);
//     }
//   } // end of CompletionListListener inner class
  
//   private UseTermListener useTermListener;
//   private UseTermListener getUseTermListener() {
//     if (useTermListener == null) useTermListener = new ComboUseTermListener();
//     return useTermListener;
//   }
//       CharacterI c = getSelectedCharacter(); // from selectionManager
//       CharFieldEnum cfe = charField.getCharFieldEnum();
//       UpdateTransaction ut = new UpdateTransaction(c,cfe,oboClass);
//       EditManager.inst().updateModel(this,ut);
  //private Ontology getOntology() { return ontology; }

  /** Return true if input String matches name of OBOClass in
   * defaultComboBoxModel - rename this? this isnt used anymore - delete?
   */
//   boolean isInCompletionList(String input) {
//     if (defaultComboBoxModel == null)
//       return false;
//     if (input == null) {
//       return false;
//     }
//     // this is wrong as it holds OBOClasses not Strings!
//     //return defaultComboBoxModel.getIndexOf(input) != -1;
//     // have to go through all OBOClasses and extract there names - bummer
//     // most likely input is selected one check that first
//     OBOClass selectedClass = getSelectedOboClass();
//     if (selectedClass != null && input.equals(selectedClass.getName()))
//       return true;
//     // selected failed(is this possible?) - try everything in the list then...
//     for (int i=0; i<defaultComboBoxModel.getSize(); i++) {
//       if (input.equals(getCompListOboClass(i).getName()))
//         return true;
//     }
//     return false;
//   }
 
    // mac bug workaround where list covers up textfield on < 12 items no scroll
    // from http://www.orbital-computer.de/JComboBox/#usage
    // it does note this may cause class cast excpetions??
    // it does cause exception when down arror is typed... hmmm...
    //setUI(new BasicComboBoxUI()); // now setting metal look & feel for whole app
//String ontology,AutoComboBox cb) {
    //private String previousModelValue=null;
      //this.ontology = ontology;
      //comboBox = cb;
         //setTableFromField(ontology);
      //t.editModel();  // or charField.editModel?
      // CharacterChangeEvent e = new CharacterChangeEvent(t);
      // OR CharEditManager.inst().updateModel(c,cfe,input,previousModelValue);
      // CEM.handleTransaction(new UT), CEM.updateModel(UT)
      // fireChangeEvent(e);

      // check if input is a real term - i think we can get away with checking
      // if in present term completion list - not sure
      // i think this is replaced by check above - make sure does the same...
//       boolean valid = isInCompletionList(oboClass); //input);
//       if (!valid)
//         return; 


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
