package phenote.gui.field;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.accessibility.Accessible;
import javax.swing.ComboBoxModel;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import phenote.gui.SearchParams;
import phenote.gui.SearchParamsI;
/** has a jcombobox that does auto completion - i had to do some tricks(hacks) to get it
    working with mouse over which doesnt come naturally to jcombobox */

public abstract class AbstractAutoCompList extends CharFieldGui {

  private JComboBox jComboBox = new JComboBox();
  private boolean changingCompletionList = false;
  private boolean doCompletion = true;
  private CompComboBoxModel compComboBoxModel;
  private boolean inTestMode = false;
  private AutoTextField autoTextField;
  private SearchParamsI searchParams = SearchParams.inst();
  private CompListSearcher compListSearcher;
  private boolean setGuiForMultiSelect = false;
  // minimum chars to type for completion to happen - from config
  private int minCompChars = 0;

  protected AbstractAutoCompList(CharField cf) {
    this(cf,0); // minCompChars = 0
  }

  protected AbstractAutoCompList(CharField cf,int minCompChars) {
    super(cf);
    setMinCompChars(minCompChars);
    init();
  }

  private void init() {
    jComboBox.setEditable(true);
    AutoTextFieldEditor autoTextFieldEditor = new AutoTextFieldEditor();
    jComboBox.setEditor(autoTextFieldEditor);
    jComboBox.setRenderer(new ResizingComboBoxRenderer());
    jComboBox.addActionListener(new ComboBoxActionListener());
    compListSearcher = new CompListSearcher(getCharField().getOntologyList());
    // init with all terms if config.showAllOnEmptyInput...
    if (getMinCompChars() == 0) {
      boolean showPopupWithComp = false; // dont show popup -
      doCompletion(showPopupWithComp);  //we dont have a gui yet, just populating
    }
  }

  public void setMinCompChars(int minChars) { minCompChars = minChars; }
  public int getMinCompChars() { return minCompChars; }

  protected Component getUserInputGui() { return jComboBox; }

  protected boolean isCompList() { return true; }

  protected SearchParamsI getSearchParams() { return searchParams; }

  protected CompListSearcher getCompListSearcher() { return compListSearcher; }

  protected AbstractAutoCompList getCompList() { return this; }

  protected boolean hasOntologyChooser() { return hasMoreThanOneOntology(); }
  
  protected boolean hasMoreThanOneOntology() {
    return getCharField().hasMoreThanOneOntology();
  }

  /** char in table changed - adjust - not needed for rel(at least not yet)
      as post comp doesnt listen to table changes (does it? should it?), 
      just term */
  protected abstract void setValueFromChar(CharacterI chr);

  /** Set text in editable text field of j combo (eg from table select) */
  public void setText(String text) {
    // do not do term completion on externally set text!
    boolean doCompletion = false;
    setText(text,doCompletion);
  }

  /** text from selecting table doesnt do completion, TestPhenote does */
  public void setText(String text, boolean doCompletion) {
    this.doCompletion = doCompletion;
    jComboBox.getEditor().setItem(text);
    this.doCompletion = true; // set back to default
  }

  /** for now just clear out gui without editing model */
  protected void setGuiForMultiSelect() {
    // flag to prevent AutoTextField.setText from setting to current term
    // is there a cleaner way to do this?? - this falg may need to be generalized
    // as may want for user being able to set to "" (non-required field gen-con)
    setGuiForMultiSelect = true; // ????
    setText("*",false); // false -> no completion, "*"?
    setGuiForMultiSelect = false;
  }

  /** Return text in text field */
  public String getText() {
    return jComboBox.getEditor().getItem().toString(); // or editor.getText() ?
  }

  

  void clear() {
    setText("");
  }

  /** this is funny for TermCompList this is a CompletionTerm, for RelationCompList this
      is a RelationTerm */
  protected Object getSelectedObject() throws OboException {
    if (compComboBoxModel == null) throw new OboException(); // ??
    Object obj = compComboBoxModel.getSelectedItem();
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
      super();
      autoTextField = new AutoTextField(); // outer instance var for testing
      editor = autoTextField; // protected editor var from BCBE
      addDocumentListener(new AutoDocumentListener());
      // call returnKeyHit - for nulling out term
      addReturnKeyListener(autoTextField);
      this.correctInputMapForEditorField(autoTextField);
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
    
    /**
     * This method replaces the AutoTextFieldEditor's InputMap with the same one
     * the current look and feel would use.  This was a problem for keyboard selection
     * when using the Quaqua look and feel.
     */
    private void correctInputMapForEditorField(JTextField field) {
      final Component systemComboBoxEditor = new JComboBox().getEditor().getEditorComponent();
      if (systemComboBoxEditor instanceof JComponent) {
        final InputMap map = ((JComponent)systemComboBoxEditor).getInputMap();
        SwingUtilities.replaceUIInputMap(field, JComponent.WHEN_FOCUSED, map);
      }
    }
    
  }


  /** AutoTextField inner class - ignores set text when in
   * changingCompletionList mode - this is the text field for the
   combo box */
  private class AutoTextField extends JTextField {

    private AutoTextField() {
      super();
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
      //this is problematic for syns & such where string is diff than term name
      // JComboBox sets this text AFTER got event and set to name
      //super.setText(text); 
      // this works as only time setText is called with cngCL false is with
      // selection - or at least so it seems
      // well that was true but now need to clear text for multi sel
      if (setGuiForMultiSelect) // flag will probably need to be more general?
        super.setText(text);
      else
        super.setText(getCurrentTermRelName()); // set to term name with syn select
      doCompletion = true;
    }

    protected void processKeyEvent(KeyEvent e) {
      super.processKeyEvent(e);
    }
  }


  /** Return the name of the current term or relation - was gonna call this
      item name but that gets confused with "items" in combo box */
  protected abstract String getCurrentTermRelName();





  /** If combo box is relationship then the items will be OBOProperties not
      OBOClasses */
  private boolean isRelationshipList() {
    return getCharField().isRelationship();
  }
  private boolean isTermList() { return !isRelationshipList(); }

  /** MAKE COMPLETION LIST FROM USER INPUT
      Populates defaultComboBoxModel with List of CompTerms */
  private void doCompletion() {
    // showPopup can hang during test but not during real run - bizarre
    doCompletion(!inTestMode);
  }

  private void doCompletion(boolean showPopup) {
    if (!doCompletion) // flag set if text filled in externally (from table sel)
      return;
    
    // too soon - text field doesnt have text yet.... hmmmm....
    String input = getText();
    // If length of input is shorter than minimum required for completion do nothing
    if (input.length() < minCompChars) return;
    // returns a list of CompletionTerms (checks if relations)
    // if input is empty will return whole list (if configged)
    //log().debug("got new completion request for input "+input+" time "+time());
    List<CompletionTerm> l = getSearchItems(input);
    //log().debug("got search items for input "+input+" milsec: "+time());
   changingCompletionList = true;
    // could just do comboBoxModel.setList(l); ???
    compComboBoxModel = new CompComboBoxModel(l);
    jComboBox.setModel(compComboBoxModel);
    changingCompletionList = false;
    if (showPopup)
      jComboBox.showPopup();//only show popup on key events actually only do comp w key
    //log().debug("put comp list in gui for input "+input+" milsec: "+time());
  }

  private long time=0;
  private long time() {
    long newTime =  System.currentTimeMillis();
    long newTimeDiff = newTime-time;
    time = newTime;
    return newTimeDiff;
  }


  // for subclasses to override - abstract?
  protected void returnKeyHit() {}

  abstract protected void setModelToNull();


  /** This is cheesy but theres a hanging bug with showPopup that only happens
      in test mode - dont know why but doesnt actually matter, so setting flag
      to turn off showpopup in test - if hangs in nontest will investigate */
  public void setTestMode(boolean inTestMode) {
    this.inTestMode = inTestMode;
  }

  protected abstract List<CompletionTerm> getSearchItems(String input);
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

  protected JList getUIJList() {
    Accessible popup = jComboBox.getAccessibleContext().getAccessibleChild(0);
    if (!(popup instanceof ComboPopup)) {
      this.log().error("Can't retrieve popup from combobox; can't do mouse overs - found instead " + popup.getClass());
      return null;
    } else {
      return ((ComboPopup)popup).getList();
    }
  }

  // for TestPhenote
  public void doMouseOver(int itemNumber) {
    //System.out.println("AutoComboBox.doMouseOver not implemented yet");
    if (!canGetUIJList()) return;
    JList jList = getUIJList();
    jList.setSelectedIndex(itemNumber);
  }



   /** Listens for actions from combo boxes and edits model/character 
   * actions come from mouse select/click of term as well as return & tab  */
   private class ComboBoxActionListener implements ActionListener {

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

      // abstract method - sets currentSelItem, returns false if not valid
      try { setCurrentValidItem(); }
      catch (OboException e) { return; } // if not valid then dont edit model

      // EDIT MODEL 
      if (editModelEnabled())
        editModel();
    }
  }

  /** The user has selected a real item (from list) record this in the subclass
      in TermCL set currentSelOboClass, in RelCL set currentSelectedRelation 
      throws ex if in fact current user input is not a valid item */
  protected abstract void setCurrentValidItem() throws OboException;


  protected abstract void editModel();

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
  public void simulateLKeyStroke() {
    autoTextField.processKeyEvent(new KeyEvent(jComboBox,KeyEvent.KEY_PRESSED,0,0,KeyEvent.VK_L,'l'));
    autoTextField.processKeyEvent(new KeyEvent(jComboBox,KeyEvent.KEY_TYPED,0,0,KeyEvent.VK_UNDEFINED,'l'));
  }

  public void simulateKeyStroke(int keyCode, char c) {
    KeyEvent k = new KeyEvent(jComboBox,KeyEvent.KEY_PRESSED,0,0,keyCode,c);
    autoTextField.processKeyEvent(k);
    k.setKeyCode(KeyEvent.KEY_RELEASED);
    autoTextField.processKeyEvent(k);
    k = new KeyEvent(jComboBox,KeyEvent.KEY_TYPED,0,0,KeyEvent.VK_UNDEFINED,c);
    autoTextField.processKeyEvent(k);
  }

  /** For TestPhenote */
  public JComboBox getJComboBox() { return jComboBox; }

  private class CompComboBoxModel implements ComboBoxModel {
    //private List<CompletionTerm>, cant do - may also be CompletionRelation!
    private List list;
    private Object selectedItem;

    private CompComboBoxModel(List l) { list = l; }

    public Object getSelectedItem() { return selectedItem; }
    public void setSelectedItem(Object anItem) { selectedItem = anItem; }
    public void	addListDataListener(ListDataListener l) {}
    public Object getElementAt(int index) { return list.get(index); }
    public int getSize() { return list.size(); }
    public void removeListDataListener(ListDataListener l) {}
  }
  
  private static class ResizingComboBoxRenderer extends BasicComboBoxRenderer {
    
    private static final String ELLIPSIS = "...";
    private static final int PADDING = 3; // this may be look-and-feel dependent
    
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      if (!(value instanceof CompletionTerm)) return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      final String name = ((CompletionTerm)value).getCompListDisplayName();
      final String suffix = ((CompletionTerm)value).getCompListDisplaySuffix();
      String displayValue;
      if (suffix.length() == 0) {
        // there are no tags, truncate as normal
        displayValue = name;
      } else {
        final int width = list.getWidth() - PADDING;     
        final FontMetrics metrics = list.getFontMetrics(list.getFont()); // change this to component (which is actually this anyway)
        //final char[] textArray = text.toCharArray();
        final String combined = name + suffix;
        if ((SwingUtilities.computeStringWidth(metrics, combined)) < width) { 
          // the whole thing will fit with the tags, don't worry about truncation
          displayValue = combined;
        } else {
          // we need to figure out how much text we can fit with an ellipsis before the tags
          final String ellipsisPlusTags = ELLIPSIS + suffix; 
          int stopIndex;
          String elided = "";
          for (stopIndex = (name.length() - 1); stopIndex > 1; stopIndex--) {
            elided = name.substring(0, stopIndex) + ellipsisPlusTags;
            final int totalWidth = SwingUtilities.computeStringWidth(metrics, elided);
            if (totalWidth < width) {
              break;
            }
          }
          displayValue = elided;
        }
      }
      return super.getListCellRendererComponent(list, displayValue, index, isSelected,
          cellHasFocus);
    }

    /* Built-in preferredSize() forces combobox to width of longest list element.
     * Override allows smaller resizing - but parent component needs to give it a reasonable size.
     */
    public Dimension getPreferredSize() {
      final Dimension dimension = super.getPreferredSize();
      dimension.width = 1;
      return dimension;
    }
  }
  
}


// GARBAGE
  /** @param editModel if false then ACB doesnt edit model directly (post comp) 
   can abstract classes have constructors - if not init() */
  //protected AbstractAutoCompList(CompListSearcher s,boolean editModel,CharField cf) {
//   protected AbstractAutoCompList(SearchParamsI sp,boolean editModel,CharField cf,
//                                  String label) {
//     super(cf,label);
//     init();
//   }
  /** Override - configureEditor is called when user selects item and sets text
      field to item selected, unfortunately this happens after the listening 
      code in this class that sets to term name (not syn name), and then the syn
      name gets set - so this is to catch & repress the subsequent syn name 
      setting - hope that makes sense heres the jdocs from JComboBox for this method:
      Initializes the editor with the specified item. It seems to be ok as far
      as i can tell to supress this method entirely - if this ends up being 
      problematic then this should be coulpled with a flag set in setCurrentValidItem
      or a related method to supress the syn coming after term set 
  this doesnt seem to be supressing anymore so i guess its ok */
//   public void configureEditor(ComboBoxEditor anEditor,Object anItem) {
//     //log().debug("configure editor called"+anItem);
//     //new Throwable().printStackTrace();
//     // it appears to be ok to supress this entirely
//     super.configureEditor(anEditor,anItem); // ??? supress
//   }

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
  /** This is cheesy but theres a hanging bug with showPopup that only happens
      in test mode - dont know why but doesnt actually matter, so if inTestMode
      then dont showpopup - if hangs in nontest will investigate 
      this actually needs to go in subclass of JCombo if we go there - this class
      no longer sublcasses JCombo - for now commenting out */
//   public void showPopup() {
//     if (inTestMode)
//       return;
//     super.showPopup();
//   }


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
    //setModel(defaultComboBoxModel);
    //defaultComboBoxModel = new DefaultComboBoxModel(v);
    //if (isRelationshipList()) v = ontology.getStringMatchRelations(input);
    //else v = getTerms(input);
    // throws IllegalStateException, Attempt to mutate in notification
    // this tries to change text field amidst notification hmmmm.....
    // this is a vector of OBOClasses
    // i think ultimately we will need to wrap the OBOClass to be able to
    // have more control over the string - cut off w ... & [syn][obs] tags
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
  /** disable completion for item being selected - otherwise a popup comes up after
      the selection - probably no harm in keeping this but this may be redundant with
      the turning off of doCompletion in inner ATF.setText which is more
      comprehensive - also we are no longer subclass of JCombo so this doesnt
      even get called - if we do need this then we need to subclass JCombo */
//   public void setSelectedItem(Object item) {
//     doCompletion = false;
//     jComboBox.setSelectedItem(item);
//     doCompletion = true;
//   }
      //boolean fiddle = KeyboardState.shouldProcess(e);
      //System.out.println("ATF.settext ["+text+"] curr term rel name "+getCurrentTermRelName());
      //if (charField!=null)
      //log().debug(charField.getName()+" AutoTextField.setText ["+text+"]"+getCurrentTermRelName());
      //new Throwable().printStackTrace();
      //getTextField().addKeyListener(new AutoKeyListener());
    //return (String)getSelectedItem();
    //if (charField!=null)log().debug(charField.getName()+" setting text ["+text+"]");
    //new Throwable().printStackTrace();
    //this.keyTyped = doCompletion; // key has to be typed for completion
  // {charField.getCharFieldEnum().getValue(chr).getOboClass() }
