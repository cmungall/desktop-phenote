package phenote.gui.field;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;

import phenote.config.Config;
import phenote.config.DataAdapterConfig;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.dataadapter.ncbi.NCBIDataAdapterI;
import phenote.dataadapter.ncbi.OMIMAdapter;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.gui.selection.SelectionManager;
import ca.odell.glazedlists.swing.EventSelectionModel;

/** fields can either be text fields for free text or combo boxes if have 
    ontology to browse - CharFieldGui does either - with get/setText - hides the
    details of the gui - just a field that gives text 
    should there be subclasses for free text, term, & relations? hmmmm 
    ListSelectionListener listening to events from table selection (make inner class?) */
public abstract class CharFieldGui implements ListSelectionListener {
  private CharField charField;
  private String label;
  /** if true then set gui but not model, for clearing on multi, default false */
  private boolean updateGuiOnly = false;
  private JButton retrieveButton;
  static int fieldHeight = 17;
  static Dimension inputSize = new Dimension(390,fieldHeight); // size of user input box
  private boolean editModel = true;
  private SelectionManager selectionManager;
  private EditManager editManager;
  protected EventSelectionModel<CharacterI> selectionModel;
  private boolean inMultipleValueState = false;
  private boolean hasChangedMultipleValues = true;
  private Color enabledColor;
  private Color disabledColor;
  private JScrollPane listScroll;
  private JList listGui;
  private ValueListModel valueListModel;
  /** flag for supressing valueChanged if comes from self */
  private boolean doingInternalEdit = false;

  /** CharFieldGui for main window not post comp box - factory method */
  public static CharFieldGui makeCharFieldGui(CharField charField,int minCompChars) {
    CharFieldGui fieldGui;
    if (charField.isTerm()) { //hasOntologies()) {
      //return new TermCompList(charField,sp,true); // enable listeners
      TermCompList t = new TermCompList(charField,minCompChars);
      //t.setSearchParams(sp);
      t.allowPostCompButton(true);
      fieldGui = t;
    }
    else if (charField.isID()) {
      fieldGui = new IdFieldGui(charField);
    }
    else if (charField.isReadOnly()) {
      fieldGui = new ReadOnlyFieldGui(charField);
    }
    else {
      FreeTextField f = new FreeTextField(charField);
      fieldGui = f;
    }
    fieldGui.setGuiForNoSelection();
    final JComponent component = fieldGui.getUserInputGui();
    if (component instanceof JComboBox) {
      // JComboBox does not correctly notify its focus listeners - need to use editor instead
      ((JComboBox)component).getEditor().getEditorComponent().addFocusListener(new FieldFocusListener(fieldGui));
    } else {
      component.addFocusListener(new FieldFocusListener(fieldGui));
    }
    return fieldGui;
  }

  /** createPostCompRelationList - will relation lists ever be in main window and if
      so will they ever have listeners enabled - maybe, probably not */
  static CharFieldGui makeRelationList(CharField cf) {
    RelationCompList r = new RelationCompList(cf);
    //r.setSearchParams(sp); // does rel really need search params?
    return r;
  }

  /** make term completion lists for post comp window (genus & diff), they dont listen
      to selection nor edit model - isolated */
  public static CharFieldGui makePostCompTermList(CharField cf,String label,
                                           int minCompChars) {
    // false - no listeners(dont edit model), false - dont add comp button
    // eventually adding comp button come from config for recursive comp
    //boolean allowPostCompBut = false;
    TermCompList t =  new TermCompList(cf,minCompChars);
    //t.setSearchParams(sp);
    // t.isInSeparateWindow(true) or t.isolate(true)??
    t.enableEditModel(false);
    t.allowPostCompButton(false); // eventually config for recurse/embed
    t.setLabel(label);
    return t;
  }



  protected CharFieldGui(CharField charField) {
    init(charField);
  }


  private void init(CharField cf) { //, FieldPanel tp) {
    charField = cf;
    //fieldPanel = tp;
    //if (!charField.hasOntologies()) initTextField(charField.getName());
    //else initCombo();
    // check queryableAdapter if charField is queryable
    addRetrieveButton();
    // enableListeners(true)
  }


  /** Get the component used for user input - text field or jCombo */
  protected abstract JComponent getUserInputGui();

  public void setListSelectionModel(EventSelectionModel<CharacterI> model) {
    this.selectionModel = model;
    this.selectionModel.addListSelectionListener(this);
    this.setValueFromChars(this.selectionModel.getSelected());
  }
  
  /** part of ListSelectionListener interface. Receives ListSelectionEvents
      from selectionModel. Table row selection causes this event to fly */
  public void valueChanged(ListSelectionEvent e) {
    // user editing field causes model change cause table change causes valueChanged
    // which then messes up selection from list, and dont need as change came from
    // self, so doingInternalEdit supresses listening to this - is there a better way
    // to do this?
    if (doingInternalEdit) return;
    this.updateGuiOnly = true;
    this.setValueFromChars(this.getSelectedChars());
    this.updateGuiOnly = false;
  }

  protected void setDoingInternalEdit(boolean doingEdit) {
    doingInternalEdit = doingEdit;
  }
  
  /** Set the gui from the model (selection) */
  protected void setValueFromChars(List<CharacterI> characters) {
    this.setForegroundColor(this.getEnabledTextColor());
    this.setInMultipleValueState(false);
    if (characters.isEmpty()) {
      this.setGuiForNoSelection();
      return;
    }
    this.getUserInputGui().setEnabled(true);
    // if all characters have same value then set to that value
    if (this.areCharactersEqualForCharField(characters, this.getCharField())) {
      this.setCharFieldValue(characters.get(0).getValue(this.getCharField()));
      updateListGui();
    } else {
      this.setMultipleValuesConditions();
    }
  }
  
  /** return true if all values for char field in all characters are the same 
      value(equal) - used for multi select */
  protected boolean areCharactersEqualForCharField(List<CharacterI> characters, CharField charField) {
    if (characters.isEmpty()) return true;
    // why final?
    //final CharFieldValue firstValue = characters.get(0).getValue(this.getCharField());
    final CharacterI firstChar = characters.get(0);
    for (CharacterI character : characters) {
      //final CharFieldValue otherValue = character.getValue(this.getCharField());
//       if (otherValue == null) {
//         if (firstValue == null) {
//           continue;
//         } else {
//           return false;
//         }
//       }
      //if (!otherValue.equals(firstValue)) {
      // fieldEquals handles lists as well as single values
      if (!character.fieldEquals(firstChar,getCharField())) {
        return false;
      }
    }
    return true;
  }
  
  protected abstract void setCharFieldValue(CharFieldValue value);
  
  protected void focusLost() {
    if (this.shouldResetGuiForMultipleValues()) {
      this.setGuiForMultipleValues();
    } 
  }
  
  protected void focusGained() {
    if (this.isInMultipleValueState()) {
      this.setGuiForMultipleValues();
      this.setHasChangedMultipleValues(false);
    }
  }

  public void setSelectionManager(SelectionManager manager) {
    this.selectionManager = manager;
  }
  
  public SelectionManager getSelectionManager() {
    if (this.selectionManager == null) {
      return SelectionManager.inst();
    } else {
    return this.selectionManager;
    }
  }
  
  public void setEditManager(EditManager manager) {
    this.editManager = manager;
    manager.addCharChangeListener(new FieldCharChangeListener());
  }
  
  public EditManager getEditManager() {
    if (this.editManager == null) {
      return EditManager.inst();
    } else {
    return this.editManager;
    }
  }
  
  protected void setMultipleValuesConditions() {
    this.setInMultipleValueState(true);
    this.setHasChangedMultipleValues(false);
    this.setGuiForMultipleValues();
  }
  
  protected boolean shouldResetGuiForMultipleValues() {
    return (this.isInMultipleValueState() && (!this.hasChangedMultipleValues()));
  }
  
  protected void setGuiForMultipleValues() {
    if (this.hasFocus()) {
      this.setForegroundColor(this.getEnabledTextColor());
      this.setText("");
    } else {
      this.setForegroundColor(this.getDisabledTextColor());
      this.setText("Multiple Values");
    }
  }
  
  protected void setGuiForNoSelection() {
    this.getUserInputGui().setEnabled(false);
    this.setText("No Selection");
  }
  
  protected Color getDisabledTextColor() {
    if (this.disabledColor == null) {
      this.disabledColor = (new JTextField()).getDisabledTextColor();
    }
    return this.disabledColor;
  }
  
  protected Color getEnabledTextColor() {
    if (this.enabledColor == null) {
      this.enabledColor = (new JTextField()).getForeground();
    }
    return this.enabledColor;
  }
  
  protected boolean isInMultipleValueState() {
    return this.inMultipleValueState;
  }

  protected void setInMultipleValueState(boolean inMultipleValueState) {
    this.inMultipleValueState = inMultipleValueState;
  }

  protected boolean hasChangedMultipleValues() {
    return this.hasChangedMultipleValues;
  }

  protected void setHasChangedMultipleValues(boolean hasChangedMultipleValues) {
    this.hasChangedMultipleValues = hasChangedMultipleValues;
  }
  
  protected void setForegroundColor(Color color) {
    this.getUserInputGui().setForeground(color);
  }
  
  protected abstract boolean hasFocus();

  protected void enableEditModel(boolean em) { editModel = em; }
  protected boolean editModelEnabled() { return editModel; }
  
  /** Main method for subclasses to edit model with current value */
  protected abstract void updateModel();
  
  /** get selected chars from selection model */
  protected List<CharacterI> getSelectedChars() {
    if (this.selectionModel == null) {
      return Collections.emptyList();
    } else {
      return this.selectionModel.getSelected(); 
    }
  }
  
  // overridden by AbstaractAutoCompList
  //protected void setSearchParams(SearchParamsI sp) {}
  

  /** no-op - overridden by term comp list - set to false for now for terms in comp
      window - resursion coming... */
  protected void allowPostCompButton(boolean allow) {}

  private void addRetrieveButton() {
  	if (!(Config.inst().hasQueryableDataAdapter() || Config.inst().hasNCBIAdapter())) return;

  	QueryableDataAdapterI qa = Config.inst().getQueryableDataAdapter(); // for now just one
  	List<NCBIDataAdapterI> naList = Config.inst().getNCBIDataAdapters();
  	//i'm hard-coding this for now...needs to be much slicker, perhaps not
  	//even the same kind of queryable adapter
  	if (qa!=null) {
  		if (qa.isFieldQueryable(getCharField().getName())) {
  			retrieveButton = new JButton("Retrieve");
  			retrieveButton.addActionListener(new RetrieveActionListener(qa));
  			//fieldPanel.addRetrieveButton(b);
  		}
  	} else if (naList.size()>0) {
  		//add the button if there is a match for the particular adapter
  		for (NCBIDataAdapterI nda : naList) {
  			if (nda.isFieldQueryable(getCharField().getName())) {
  				retrieveButton = new JButton(new ImageIcon("images/ncbi_icon.png"));  //would like this to be an action
  				retrieveButton.setPreferredSize(new Dimension(35,35));
  				retrieveButton.addActionListener(new NCBIActionListener(nda));
  				retrieveButton.setToolTipText("Query NCBI for matching ids");
  			}
  		}
  	}
  }

  boolean hasRetrieveButton() { return retrieveButton != null; }
  JButton getRetrieveButton() { return retrieveButton; }

  private class NCBIActionListener implements ActionListener {
    NCBIDataAdapterI nda;
    private NCBIActionListener(NCBIDataAdapterI q) { nda = q; }
    public void actionPerformed(ActionEvent e) {
        selectionManager.selectID(this, getText(), nda.getName());
    }
  }
  
  private class RetrieveActionListener implements ActionListener {
    QueryableDataAdapterI qda;
    private RetrieveActionListener(QueryableDataAdapterI q) { qda = q; }
    public void actionPerformed(ActionEvent e) {
      try {
        String queryValue = getText();
        if (isTermCompList()) { queryValue = getCurrentOboClass().getID(); }
        for (String group : qda.getQueryableGroups()) {
          CharacterListI cl = qda.query(group,charField.getName(),queryValue);
          //if (cl == null) if null shouldve thrown ex - check anyways?
          //notifyNewCharList(cl);
          // check if unsaved data - if so ask user if wants to save/load
          CharacterListManager.getCharListMan(group).setCharacterList(this,cl);
        }
      }
      catch (CharFieldGuiEx ex) {
          JOptionPane.showMessageDialog(null,ex.getMessage(),"Retrieve Error",
                                        JOptionPane.ERROR_MESSAGE); }
      catch (DataAdapterEx ey) {
          JOptionPane.showMessageDialog(null,ey.getMessage(),"Retrieve Error",
                                        JOptionPane.ERROR_MESSAGE);

      }
    }
  }
  
  private static class FieldFocusListener implements FocusListener {
    
    private CharFieldGui field;
    
    public FieldFocusListener(CharFieldGui fieldGui) {
      this.field = fieldGui;
    }
    
    public void focusGained(FocusEvent e) {
      if (e.isTemporary()) {
        return;
      }
      this.field.focusGained();
    }
    
    public void focusLost(FocusEvent e) {
      if (e.isTemporary()) {
        return;
      }
      this.field.focusLost();
    }
  }

  // FreeTextField.updateModel uses this
  protected boolean updateGuiOnly() { return updateGuiOnly; }
  protected void setUpdateGuiOnly(boolean u) { updateGuiOnly = u; }
  

  /** edits from post comp come in here i believe (term info used to but now
      thats done with UseTermEvent), also edits on the model from EditManager 
      but they get tossed i think as its either the same source or not for this
      char field */
  private class FieldCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      // check charField is this char field
      updateListGui();
      if (e.getSource() != CharFieldGui.this && e.isUpdateForCharField(charField)) {
        // i think all we need to do is setText to synch with model
        // for complist dont we also need to set its model (not just text??)
        //setText(e.getValueString()); // might as well just synch with model
        // so in the case of multi select presumably they have all been modified in
        // the same manner so sufficient to just synch with 1st one
        // dont edit model if undo (model already edited!)
        // wait any char change event shouldnt change model as it came from model change
        //boolean editModel = !e.isUndo();
        //boolean editModel = false;
        // maybe setModelUpdateMode(false) ??

        // is updateGuiOnly still needed here? dont think so - only for free text
        // and this only gets post comp term updates ???
        //updateGuiOnly = true; // disable model editing - a better way to do this???
//        setValueFromChar(getFirstSelectedChar());
        //updateGuiOnly = false; // reenable model editing
      }
    }
  }

  /** for testing and internal use - overridden */
  AbstractAutoCompList getCompList() { return null; } // overridden

  // hasOntology? overridded by AbstractAutoCompList
  boolean isCompList() { return false; }// isCompList; }

//   private boolean isRelationshipList() {
//     return charField.isRelationship();
//   }

  protected TermCompList getTermComp() {
    // throw ex if null?
    //return termCompList;
    return null; // overridden by TermCompList
  }



  protected void setLabel(String label) {
    this.label = label;
  }

  String getLabel() {
    if (label == null) {
//      if (charField.hasMoreThanOneOntology() || !charField.hasOntologies())
      label = charField.getName();
//       else
//         label = charField.getFirstOntology().getName();
    }
    return label;
  }


  protected abstract void setText(String text);
  protected abstract String getText();
 

  /** no-op overridden by TermCompList */
  protected void setOboClass(OBOClass term) {}

  /** for auto combos (ontol) for relationships (post comp rel)
   no-op overriddedn by RelationCompList */
  void setRel(OBOProperty rel) {}

  CharFieldEnum getCharFieldEnum() { return charField.getCharFieldEnum(); }
  protected CharField getCharField() { return charField; }

  public static class CharFieldGuiEx extends Exception {
    protected CharFieldGuiEx(String m) { super(m); }
  }

  /** overridden by term comp list */
  OBOClass getCurrentOboClass() throws CharFieldGuiEx {
    throw new CharFieldGuiEx("Field has no OBO Class");
  }

  /** overridden by RelationCompList */
  OBOProperty getCurrentRelation() throws CharFieldGuiEx {
    throw new CharFieldGuiEx("Field has no Relation");
  }

  protected boolean isTermCompList() {
    return false; // overridden by term comp list
  }

  /** for post comp gui to set ontol chooser - overridden by term comp */
  void setOntologyChooserFromTerm(OBOClass term) {}
    
  protected boolean hasOntologyChooser() { return false; }
  protected JComboBox getOntologyChooser() { return null; }

  /** Overridden by TermCompList */
  protected boolean hasCompButton() { return false; }
  /** Overridden by TermCompList */
  protected JButton getCompButton() { return null; }

  /** should get this from config... stub for now */
  protected boolean hasListGui() {
    return charField.isList();
  }
  /** JList? initialize if configged
   list gui is the list that is displayed if the field is multi valued - takes
   more than one value - NOT the drop down list */
  protected JComponent getListGui() {
    if (listScroll == null) {
      listGui = new JList();
      listGui.setModel(getValueListModel());
      listGui.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      listScroll = new JScrollPane(listGui);
      listScroll.setMinimumSize(new Dimension(90,40)); // w,h
      listScroll.setPreferredSize(new Dimension(130,60));
    }
    return listScroll;
  }

  private ValueListModel getValueListModel() {
    if (valueListModel == null) 
      valueListModel = new ValueListModel();
    return valueListModel;
  }

  JButton getListDelButton() {
    JButton b = new JButton("DEL");
    b.addActionListener(new DeleteListActionListener());
    return b;
  }

  private class DeleteListActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      if (listGui.isSelectionEmpty()) return;
      int i = listGui.getSelectedIndex();
      valueListModel.delete(i);
    }
  }

  private void updateListGui() {
    if (!hasListGui()) return;
    List<CharacterI> sel = getSelectedChars();
    if (sel==null || sel.size()==0) {
      valueListModel.clear();
      return;
    }
    else if (sel.size() > 1) {
      return; // set to multiple values, todo: check if lists are the same
    }
    else {
      CharacterI c = sel.get(0); // sole selection
      List<CharFieldValue> vals = c.getValue(getCharField()).getCharFieldValueList();
      getValueListModel().setList(vals);
    }
  }
  
  private class ValueListModel extends AbstractListModel {
    private List<CharFieldValue> charValueList = new ArrayList<CharFieldValue>();
    private void setList(List<CharFieldValue> l) { 
      if (l == null) clear();
      else charValueList = new ArrayList<CharFieldValue>(l); // clone
      fireContentsChanged(this,0,getSize());
    }
    public Object getElementAt(int index) {
      if (charValueList==null) return null;
      return charValueList.get(index);
    }
    public int getSize() {
      if (charValueList==null) return 0;
      return charValueList.size();
    }
    private void clear() {
      if (charValueList==null) return;
      //charValueList.clear();
      charValueList = new ArrayList<CharFieldValue>();
      fireContentsChanged(this,0,getSize()); //update gui
    }
    private void delete(int i) {
      //FieldListItemDeleteTrans t = new FieldListItemDeleteTrans
      if (i<0 || i>getSize()) return;
      List<CharacterI> sel = getSelectedChars();
      if (sel==null || sel.isEmpty()) return;
      if (sel.size() > 1) log().error("Cant delete list items in multi select");
      CharacterI chr = sel.get(0);
      CharFieldValue old = charValueList.get(i);
      getEditManager().deleteFromValList(this,chr,charField,old);
    }
  }
  
  
  /** no op - override in term completion gui */
  public void setMinCompChars(int minCompChars) {}
  public int getMinCompChars() { return 0; }

  private static Logger log() {
    return Logger.getLogger(CharFieldGui.class);
  }
}


// GARBAGE
//   private RelationCompList getRelComp() {
//     //return relCompList;
//     return null; // overridden
//   }

  // hardwired in term & rel subclasses now
//   void enableTermInfoListening(boolean enable) {
//     if (!isCompList()) return;
//     getCompList().enableTermInfoListening(enable);
//   }

  //JLabel getLabelGui() { return new JLabel

//  private void initCombo() { //, Container parent) {
    //isCompList = true;

    //String name = charField.getFirstOntology().getName();JLabel label = 
    //fieldPanel.addLabel(getLabel(),charField.hasMoreThanOneOntology());

    // if has more than one ontology(entity) then add ontology choose list
//     if (charField.hasMoreThanOneOntology())
//       initOntologyChooser(charField);

    //createCompList(); // comp list now makes ontologyChooser (if >1 ont)
    //fieldPanel.addFieldGui(getCompList());

    //getCompList().setCharField(charField);

