package phenote.gui.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractButton;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldValue;
import phenote.gui.CorrectLineBorderCellEditor;
import phenote.gui.SearchParams;
import phenote.gui.SearchParamsI;
/** has a jcombobox that does auto completion - i had to do some tricks(hacks) to get it
    working with mouse over which doesnt come naturally to jcombobox */

public abstract class AbstractAutoCompList extends CharFieldGui {

  private JComboBox jComboBox;
  private boolean changingCompletionList = false;
  private boolean settingTextExternally = false;
  private CompComboBoxModel compComboBoxModel;
  private boolean inTestMode = false;
  private AutoTextField autoTextField;
  private SearchParamsI searchParams = SearchParams.inst();
  private CompListSearcher compListSearcher;
  private boolean actionHappened = true; // JComboBox sets value twice upon mouse selection - second time is wrong item
  // minimum chars to type for completion to happen - from config
  private int minCompChars = 0;
  private Object highlightedItem; // last item highlighted in popup menu of JComboBox

  protected AbstractAutoCompList(CharField cf) {
    this(cf,0); // minCompChars = 0
  }

  protected AbstractAutoCompList(CharField cf,int minCompChars) {
    super(cf);
    setMinCompChars(minCompChars);
    init();
  }

  private void init() {
    this.getJComboBox().setEditable(true);
    AutoTextFieldEditor autoTextFieldEditor = new AutoTextFieldEditor();
    this.getJComboBox().setEditor(autoTextFieldEditor);
    this.getJComboBox().setRenderer(new ResizingComboBoxRenderer());
    this.getJComboBox().addActionListener(new ComboBoxActionListener());
    this.getUIJList().addListSelectionListener(new PopupListListener());
    compListSearcher = new CompListSearcher(getCharField().getOntologyList());
    // init with all terms if config.showAllOnEmptyInput...
    // this can take a while (post comp gui) do on demand with popup menu listener
//     if (getMinCompChars() == 0) {
//       boolean showPopupWithComp = false; // dont show popup -
//       doCompletion(showPopupWithComp);  //we dont have a gui yet, just populating
//     }
    getJComboBox().addPopupMenuListener(new CompPopupMenuListener());
    //if (hasArrowButton())
    //getArrowButton().addActionListener(new ArrowButtonActionListener());
    //getJComboBox().addKeyListener(new ReturnKeyListener());
    //autoTextField.getTextField().addKeyListener(new ReturnKeyListener());
  }

  @Override
  public TableCellEditor getTableCellEditor() {
    final DefaultCellEditor editor = new CorrectLineBorderCellEditor(this.getJComboBox());
    editor.setClickCountToStart(2);
    return editor;
  }

  public void setMinCompChars(int minChars) { minCompChars = minChars; }
  public int getMinCompChars() { return minCompChars; }

  protected JComponent getUserInputGui() {
    return this.getJComboBox();
  }
  
  public JComboBox getJComboBox() {
    if (this.jComboBox == null) {
      this.jComboBox = new JComboBox();
    }
    return this.jComboBox;
  }

  protected boolean isCompList() { return true; }

  protected SearchParamsI getSearchParams() { return searchParams; }

  public CompListSearcher getCompListSearcher() { return compListSearcher; }

  protected AbstractAutoCompList getCompList() { return this; }

  protected boolean hasOntologyChooser() { return hasMoreThanOneOntology(); }
  
  protected boolean hasMoreThanOneOntology() {
    return getCharField().hasMoreThanOneOntology();
  }
  
  protected abstract void setCharFieldValue(CharFieldValue value);

  protected boolean hasFocus() {
    return this.getJComboBox().getEditor().getEditorComponent().hasFocus();
  }
  
  protected void setForegroundColor(Color color) {
    this.getJComboBox().setForeground(color); // have to do this with Mac UI
    this.getJComboBox().getEditor().getEditorComponent().setForeground(color); // have to do this with Metal UI
  }
  
  /** Set text in editable text field of j combo (eg from table select) */
  public void setText(String text) {
    // do not do term completion on externally set text!
    boolean settingTextExternally = true;
    setText(text,settingTextExternally);
  }

  /** text from selecting table doesnt do completion, TestPhenote does */
  public void setText(String text, boolean settingTextExternally) {
    this.settingTextExternally = settingTextExternally;
    jComboBox.getEditor().setItem(text);
    this.settingTextExternally = false; // set back to default
  }

  /** Return text in text field */
  public String getText() {
    return jComboBox.getEditor().getItem().toString(); // or editor.getText() ?
  }
  
  protected CharFieldValue getCharFieldValue() {
    try {
      return this.getCharField().makeValue(null, this.getText());
    } catch (CharFieldException e) {
      log().error("Couldn't create charfieldvalue", e);
    }
    return CharFieldValue.emptyValue(null, this.getCharField());
  }

  void clear() {
    setText("");
  }

  /** this is funny for TermCompList this is a CompletionTerm, for RelationCompList this
      is a RelationTerm - with new OboObject interface these should be merged! 
      this returns a String if the user has not selected a term in the list yet 
      the String is the current User input - rename this! 
      peculiar that the method called is getSelectedItem as the String was 
      never actually selected
      so with KeyListener on JTextField when get key selectedObj is a null state
      so returns null if so - can get text straight from TextField in this case
      if need it
  */
  protected Object getSelectedObject() throws OboException {
    if (compComboBoxModel == null) throw new OboException(); // ??
    Object obj = compComboBoxModel.getSelectedItem();
    // with key listener obj hasnt been set to String yet, so return null
    //if (obj == null) throw new OboException();
    return obj;
  }

  /** Get first object in comp list, CompTerm for TermCompList, RelationTerm for
      RelCompList (silly - merge!) - throws ex if list is empty */
  protected Object getFirstCompListObj() throws OboException {
    if (compComboBoxModel == null) throw new OboException();
    if (compComboBoxModel.isEmpty()) throw new OboException();
    return compComboBoxModel.getElementAt(0);
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
      editor.setForeground(java.awt.Color.RED);
      addDocumentListener(new AutoDocumentListener());
      this.correctInputMapForEditorField(autoTextField);
      autoTextField.addKeyListener(new ReturnKeyListener());
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
      settingTextExternally = true;
      //this is problematic for syns & such where string is diff than term name
      // JComboBox sets this text AFTER got event and set to name
      //super.setText(text); 
      // this works as only time setText is called with cngCL false is with
      // selection - or at least so it seems
      // well that was true but now need to clear text for multi sel
      if (AbstractAutoCompList.this.isInMultipleValueState() || AbstractAutoCompList.this.getSelectedChars().isEmpty()) {
        super.setText(text);
      }
      else
        super.setText(getCurrentTermRelName()); // set to term name with syn select
      settingTextExternally = false;
    }

    protected void processKeyEvent(KeyEvent e) {
      super.processKeyEvent(e);
    }
  }


  /** Return the name of the current term or relation - was gonna call this
      item name but that gets confused with "items" in combo box */
  protected abstract String getCurrentTermRelName();





//   /** If combo box is relationship then the items will be OBOProperties not
//       OBOClasses */
//   private boolean isRelationshipList() {
//     return getCharField().isRelationship();
//   }
//   private boolean isTermList() { return !isRelationshipList(); }

  /** MAKE COMPLETION LIST FROM USER INPUT
      Populates defaultComboBoxModel with List of CompTerms */
  private void doCompletion() {
    // showPopup can hang during test but not during real run - bizarre
    //log().debug("Do completion");
    doCompletion(!inTestMode);
  }

  private void doCompletion(boolean showPopup) {
    doCompletion(showPopup,true); // thread
  }

  /** This completion is threaded */
  private void doCompletion(boolean showPopup, boolean thread) {
    //this.setHasChangedMultipleValues(true);
    if (settingTextExternally) // flag set if text filled in externally (from table sel)
      return;
    // i think if we are setting text externally(from selection) we dont want to set
    // has changed multiple values do we?  else multisel will cause edit in CmBxAcLstnr
    // which can cause null values to go into list (which should be caught anyways)
    this.setHasChangedMultipleValues(true);
    // too soon - text field doesnt have text yet.... hmmmm.... ??
    String input = getText();
    // If length of input is shorter than minimum required for completion do nothing
    // disabling this - with threading dont need - and its buggy
    //if (input.length() < minCompChars) return;
    // returns a list of CompletionTerms (checks if relations)
    // if input is empty will return whole list (if configged)
    //log().debug("got new completion request for input "+input+" time "+time());
    listSearchListener.showPopup = showPopup;
    // fires thread
    /*List<CompletionTerm> l =*/ 
    getSearchItems(input,listSearchListener,thread);
    //log().debug("got search items for input "+input+" milsec: "+time());
//    changingCompletionList = true;
//     // could just do comboBoxModel.setList(l); ???
//     compComboBoxModel = new CompComboBoxModel(l);
//     jComboBox.setModel(compComboBoxModel);
//     changingCompletionList = false;
//     if (showPopup)
//       jComboBox.showPopup();//only show popup on key events actually only
    //log().debug("put comp list in gui for input "+input+" milsec: "+time());
  }

  private ListSearchListener listSearchListener = new ListSearchListener();

  /** This is where the CompListSearcher results get returned
      from thread */
  private class ListSearchListener implements SearchListener {
    private boolean showPopup = true;
    // could have CompletionItem superclass for rel & term?? yes!
    public void newResults(List results) {
      changingCompletionList = true;
      // could just do comboBoxModel.setList(l); ???
      compComboBoxModel = new CompComboBoxModel(results);
      jComboBox.setModel(compComboBoxModel);
      changingCompletionList = false;
//      if (showPopup && jComboBox.isPopupVisible()) { //only show popup on key events actually only do com
      if (showPopup) { //only show popup on key events actually only do com
        doingPopupForCompletion = true; // otherwise doComp endless loop
        try {
          jComboBox.showPopup(); // Might throw exception if we're faking the selection from the combobox
        } catch (Exception e) {}
        doingPopupForCompletion = false;
      }
      //jComboBox.revalidate();
      //jComboBox.repaint();
      //getUIJList().repaint();
      //compRepaint(jComboBox);
      //compRepaint();
      comboPopupRepaint();
    }
  }

  private void comboPopupRepaint() {
    if (!hasComboPopup()) {
      log().error("no combo popup to repaint");
      return;
    }
    ComboPopup p = getComboPopup();
    if (p instanceof Component) ((Component)p).repaint();
    else log().error("Combo Popup not a component, cant repaint");
  }


  private long time=0;
  private long time() {
    long newTime =  System.currentTimeMillis();
    long newTimeDiff = newTime-time;
    time = newTime;
    return newTimeDiff;
  }

  abstract protected void setModelToNull();


  /** This is cheesy but theres a hanging bug with showPopup that only happens
      in test mode - dont know why but doesnt actually matter, so setting flag
      to turn off showpopup in test - if hangs in nontest will investigate */
  public void setTestMode(boolean inTestMode) {
    this.inTestMode = inTestMode;
  }

  // no longer returns List<CompletionTerm>, a thread sends list to listener
  protected abstract void getSearchItems(String input,SearchListener l,boolean thread);
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
    if (!hasComboPopup()) return null;
    return getComboPopup().getList();
  }
  
  private boolean hasComboPopup() { return getComboPopup() != null; }

  /** compbo popup is plaf, and not a child component - only way i see to get
      it is via Accessible - wierd */
  private ComboPopup getComboPopup() {
    //Accessible popup = jComboBox.getAccessibleContext().getAccessibleChild(0);
    AccessibleContext ac = jComboBox.getAccessibleContext();
    for (int i=0; i<ac.getAccessibleChildrenCount(); i++) {
      Accessible a = ac.getAccessibleChild(i);
      if (a instanceof ComboPopup)
        return (ComboPopup)a;
    }
    // no ComboPopup found... exception?
    log().error("Can't retrieve popup from combobox; can't do mouse overs");
    return null; // ex?
  }

  /** This is the button pressed to see the list
      so in theory youre not supposed to delve into the innards of the jcombo
      but in practice ya need to, need to know when that button is pressed when the
      user hasnt entered anything, and AbstractButton is not plaf specific (hopefully) */
  private AbstractButton getArrowButton() {
    for (Component c : getJComboBox().getComponents()) {
      if (c instanceof AbstractButton) return (AbstractButton)c; 
    }
    return null; // ex?
  }

  private boolean hasArrowButton() { return getArrowButton() != null; }

  private class ArrowButtonActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      log().debug("arrow has been pressed jcombo vis? "+getJComboBox().isPopupVisible());
      if (!getJComboBox().isPopupVisible()) return;
      getJComboBox().hidePopup(); // ???
      if (getText() == null || getText().length() == 0) {
        doCompletion(true); // --> doNonThreadedCompletion
      }      
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
   * actions come from mouse select/click of term in comp list
   as well as return & tab
   and loss of focus - action command can distinguish between selection
   (comboBoxChanged) & the others(comboBoxEdited)
   but there is no way to distinguish between loss of focus & hitting return - in
   other words action events are rather crude! what we care about are selection
   and return - but NOT loss of focus - but cant gleen out focus from return */
   private class ComboBoxActionListener implements ActionListener {

    private ComboBoxActionListener() {}
    public void actionPerformed(ActionEvent e) {
//      log().debug("AbstractAutoCompList: Action performed: " + e); // DEL
      actionHappened = true;
      // comboBoxChanged-> user has selected from list, (cdEdited-> user has edited text box)
      // comboBoxChanged is also triggered if user simply double-clicked the combo box
      if (e.getActionCommand().equals("comboBoxChanged")) {
        AbstractAutoCompList.this.setHasChangedMultipleValues(true);
      }
      if (e.getActionCommand().equals("comboBoxEdited")) {
        // user pressed Return to send combobox action
        // JComboBox is buggy and won't set the correct item using keyboard,
        // so we force it to set the item that was highlighted in the menu
        // this is kept up to date by PopupListListener
        getJComboBox().setSelectedItem(highlightedItem);
      }

      if (AbstractAutoCompList.this.shouldResetGuiForMultipleValues()) {
        AbstractAutoCompList.this.setGuiForMultipleValues();
        return;
      } else {
        // At some point I tried "true", but it doesn't seem to be needed.  --NH, 4/15
//        AbstractAutoCompList.this.updateModel(true);
//      log().debug("actionPerformed: about to call updateModel"); // DEL
        AbstractAutoCompList.this.updateModel();
      }
    }
  }
 

  /** this listens for return key and edits model with top hit in comp list.
      Would like it to also hear tab, but tab doesn't seem to trigger keyPressed--tab
      changes the focus to the next field, so we have to detect that via focusLost
      (defined in TermCompList). */
  private class ReturnKeyListener extends KeyAdapter {
    public void keyPressed(KeyEvent e) {
//      log().debug("keyPressed(" + e + "): keycode = " + e.getKeyCode() + ", keyForTopPick = " + keyForTopPick(e)); // DEL
      if (!keyForTopPick(e)) return; // tab?
//      log().debug("AbstractAutoCompList.keyPressed: ret hit: " + e); // DEL
      boolean useTopHit = true;
      updateModel(useTopHit); // use top hit in search list
    }
  }

  private boolean keyForTopPick(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_ENTER) return true;
    // todo - tab doesnt go through i think because of focus loss?
    // Right, can't detect tab here--it's not a KeyEvent.  --NH, 6/10/10
    return e.getKeyCode() == KeyEvent.VK_TAB;
  }

  /** The user has selected a real item (from list) record this in the subclass
      in TermCL set currentSelOboClass, in RelCL set currentSelectedRelation 
      throws ex if in fact current user input is not a valid item
  dont think this needs to be in super class */
  //protected abstract void setCurrentValidItem() throws OboException;


  /** updateModel only if have selected from list */
  protected void updateModel() {
    boolean useTopHit = false;
    updateModel(useTopHit);
  }
  protected abstract void updateModel(boolean useTopHit);

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


  private class CompComboBoxModel implements ComboBoxModel {
    //private List<CompletionTerm>, cant do - may also be CompletionRelation!
    private List<Object> list;
    private Object selectedItem;

    private CompComboBoxModel(List<Object> l) { list = l; }

    public Object getSelectedItem() { return selectedItem; }
    public void setSelectedItem(Object anItem) {
      // this is incorrectly called twice by JComboBox before action is sent
      // first time has correct value, second time wrong value
      if (actionHappened) {
      selectedItem = anItem;
      actionHappened = false;
      }
    }
    public void	addListDataListener(ListDataListener l) {}
    public Object getElementAt(int index) { return list.get(index); }
    public int getSize() { return list.size(); }
    private boolean isEmpty() { return getSize()==0; }
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

  private boolean doingPopupForCompletion = false;

  /** If user hits arrow on completion field with nothing typed in, all terms
      should then be listed */
  private class CompPopupMenuListener implements PopupMenuListener {
    public void popupMenuCanceled(PopupMenuEvent e) {}
    public void	popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
    public void	popupMenuWillBecomeVisible(PopupMenuEvent e) {
      //log().debug("popup will become visible "+getText()+" disable? "+doingPopupForCompletion+" popup vis? "+getJComboBox().isPopupVisible());
      if (doingPopupForCompletion) return;
      if (getText() != null && getText().length() > 0) 
        return;
      // popup already coming dont do another
      boolean showPopup = false;
      // dont do threaded as need to wait for results before continuing with popup
      // if threaded and popup then its blinky (2 popups, 1st one is blank)
      boolean doThreaded = false;
      doCompletion(showPopup,doThreaded);
    }
  }
  
  /**
   * Keeps track of the item last highlighted in the combobox menu. 
   * This is needed because of a bug in JComboBox which selects the 
   * wrong item when choosing via keyboard.
   */
  private class PopupListListener implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e) {
      Object source = e.getSource();
      if (!(source instanceof JList)) {
        log().error("Source of combo box mouse over event is not JList "
                + source.getClass());
        return;
      }
      JList jList = (JList) source;
      highlightedItem = jList.getSelectedValue();
    }
    
  }
  
}


