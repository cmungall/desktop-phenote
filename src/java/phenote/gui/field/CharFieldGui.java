package phenote.gui.field;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
import org.obo.datamodel.OBOProperty;

import phenote.config.Config;
import phenote.config.xml.FieldDocument.Field;
import phenote.dataadapter.CharacterListManager;
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.ImageManager;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.dataadapter.ncbi.NCBIDataAdapterI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.gui.actions.ResponderChainAction;
import phenote.gui.selection.SelectionManager;
import phenote.util.FileUtil;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.swing.EventSelectionModel;

/** fields can either be text fields for free text or combo boxes if have 
    ontology to browse - CharFieldGui does either - with get/setText - hides the
    details of the gui - just a field that gives text 
    ListSelectionListener listening to events from table selection (make inner class?)
    subclasses: TermCompList, FreeTextField,... */
public abstract class CharFieldGui implements ListEventListener<CharacterI> {
  private CharField charField;
  private String label;
  /** if true then set gui but not model, for clearing on multi, default false */
  private boolean updateGuiOnly = false;
  private JButton retrieveButton;
  private JButton loadImageButton;
  private JButton browseImageButton;
  private JButton editButton;
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
  private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
  /** bean from xml conguration for field */
  private Field fieldXmlBean;
  private JLabel listMessage;

  /** CharFieldGui for main window not post comp box - factory method, make appropriate
      CFG subclass from type of charField - minCompChars is not used at moment - may
      come back */
  public static CharFieldGui makeCharFieldGui(CharField charField,int minCompChars) {
    CharFieldGui fieldGui;
    if (charField.isTerm()) {
      TermCompList t = new TermCompList(charField,minCompChars);
      t.checkPostCompButton(true); // check if field has post comp
      fieldGui = t;
    }
    else if (charField.isID()) {
//      log().debug("isID: " + charField); // DEL
      fieldGui = new IdFieldGui(charField);
    }
    // this assumes all read only are free text - this will prob need refactoring
    else if (charField.isReadOnly() || charField.isComparison()
             || charField.isAutoAnnotId()) {
      fieldGui = new ReadOnlyFieldGui(charField);
    } else if (charField.isPickList()) {
      fieldGui = new PickListFieldGui(charField);
    } else if (charField.isList() && charField.isCompound()) {
      fieldGui = new CharacterListFieldGui(charField);
    }
    //else if (charField.isAutoAnnotId()){fieldGui=new AutoAnnotIdFieldGui(charField);
    else {
      FreeTextField f = new FreeTextField(charField);
      fieldGui = f;
    }
    fieldGui.setGuiForNoSelection();
    final JComponent component = fieldGui.getUserInputGui();
    // set a property so the the field gui can handle responder chain commands sent to component
    component.putClientProperty(ResponderChainAction.CLIENT_PROPERTY, fieldGui);
    FieldFocusListener fieldFocusListener = new FieldFocusListener(fieldGui);
    if (component instanceof JComboBox) {
      // JComboBox does not correctly notify its focus listeners - need to use editor instead
      ((JComboBox)component).getEditor().getEditorComponent().addFocusListener(fieldFocusListener);
    } else {
      component.addFocusListener(fieldFocusListener);
    }
    return fieldGui;
  }

  /** createPostCompRelationList - will relation lists ever be in main window and if
      so will they ever have listeners enabled - maybe, probably not */
  public static RelationCompList makeRelationList(CharField cf) {
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
    t.checkPostCompButton(false); // todo: false -> config for recurse/embed
    t.setLabel(label);
    return t;
  }



  protected CharFieldGui(CharField charField) {
    init(charField);
  }


  private void init(CharField cf) { //, FieldPanel tp) {
    charField = cf;
    fieldXmlBean = charField.getFieldXmlConfigBean();
    // check queryableAdapter if charField is queryable
    addRetrieveButton();
    // enableListeners(true)
  }


  /** Get the component used for user input - text field or jCombo */
  protected abstract JComponent getUserInputGui();

  public void setListSelectionModel(EventSelectionModel<CharacterI> model) {
    if (this.selectionModel != null) this.selectionModel.getSelected().removeListEventListener(this);
    this.selectionModel = model;
    if (this.selectionModel != null) {
      this.selectionModel.getSelected().addListEventListener(this);
      this.setValueFromChars(this.selectionModel.getSelected());
    }
  }
  
  /** part of ListEventListener interface. Receives ListEvents
  from selectionModel's EventList. Table row selection causes this event to fly */
  public void listChanged(ListEvent<CharacterI> listChanges) {
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
    this.setEnabledState();
    this.setInMultipleValueState(false);
    if (characters.isEmpty()) {
      this.setGuiForNoSelection();
      return;
    }
    // if all characters have same value then set to that value
    if (this.areCharactersEqualForCharField(characters, this.getCharField())) {
      this.setCharFieldValue(characters.get(0).getValue(this.getCharField()));
    } else {
      this.setMultipleValuesConditions();
    }
    // in both cases update list gui needs calling, it sorts it out
    updateListGui();
  }

  protected void setEnabledState() {
    this.setForegroundColor(this.getEnabledTextColor());
    //this.getUserInputGui().setEnabled(true);
    getUserInputGui().setEnabled(isConstraintEnabled());
  }

  /** check enabling constraints, return true if dont have constraints or constraints
      say ok to enable. constraint is like only enable if field X has a value...
      currently this is hardwired to flybase genotype maker, todo: make configurable*/
  private boolean isConstraintEnabled() {
    // hardwire for now - make this configurable!
    // LA2 requires either LA1 or NLA to be filled in to be active
    if (charField.isField("LA2")) {
      for (CharacterI c : getSelectedChars()) {
        if (!c.hasValue("LA1") && !c.hasValue("NLA"))
          return false;
      }
    }
    // same constraint for ACC
    if (charField.isField("ACC")) {
      for (CharacterI c : getSelectedChars()) {
        if (!c.hasValue("LA1") && !c.hasValue("NLA"))
          return false;
      }
    }

    return true; // passed all constraints
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
  
  
  /**
   * Returns the current value as a "character-independent" value (character is null).
   */
  protected abstract CharFieldValue getCharFieldValue();
  
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
  
  /**
   * This method is called by a ResponderChainAction.  It commits the 
   * currently edited value, then causes the next character to be selected
   * so that the user can keep editing the same field for each character.
   */
  public void commitAndSelectNext() {
    this.updateModel();
    if (this.selectionModel == null) return;
    final int currentMax = this.selectionModel.getMaxSelectionIndex();
    if (currentMax > -1) {
      try {
        this.selectionModel.setSelectionInterval(currentMax + 1, currentMax + 1);
      } catch (IndexOutOfBoundsException e) {
        // no big deal, nothing more to select
      }
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
    if (this.editManager != null) {
      manager.addCharChangeListener(new FieldCharChangeListener());
    }
  }
  
  public EditManager getEditManager() {
    if (this.editManager == null) {
      return EditManager.inst();
    } else {
    return this.editManager;
    }
  }
  
  public abstract TableCellEditor getTableCellEditor();
  
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
  protected void checkPostCompButton(boolean allow) {}

  private void addRetrieveButton() {
  	if (!(Config.inst().hasQueryableDataAdapter() || Config.inst().hasNCBIAdapter())) return;

  	QueryableDataAdapterI qa = Config.inst().getQueryableDataAdapter(); // for now just one
  	List<NCBIDataAdapterI> naList = Config.inst().getNCBIDataAdapters();
  	//i'm hard-coding this for now...needs to be much slicker, perhaps not
  	//even the same kind of queryable adapter
  	if (qa!=null) {
//          log().debug("isFieldQueryable(" + getCharField().getName() + ") = " + qa.isFieldQueryable(getCharField().getName())); // DEL
  		if (qa.isFieldQueryable(getCharField().getName())) {
  			retrieveButton = new JButton("Retrieve");
  			retrieveButton.addActionListener(new RetrieveActionListener(qa));
  			//fieldPanel.addRetrieveButton(b);
  		}
  	} else if (naList.size()>0) {
  		//add the button if there is a match for the particular adapter
  		for (NCBIDataAdapterI nda : naList) {
//                  log().debug("nda.isFieldQueryable(" + getCharField().getName() + ") = " + nda.isFieldQueryable(getCharField().getName())); // DEL
  			if (nda.isFieldQueryable(getCharField().getName())) {
			    try {
				ImageIcon icon = new ImageIcon(FileUtil.findUrl("images/ncbi_icon.png"));
			        retrieveButton = new JButton(icon);  //would like this to be an action
			    } catch (FileNotFoundException ex) { 
			    retrieveButton = new JButton();
			    } 
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
//      log().debug("Calling selectID " + getText() + ", " +  nda.getName()); // DEL
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

  boolean hasLoadImageButton() { return getLoadImageButton() != null; }
  JButton getLoadImageButton() { 
    if (!loadImageButtonConfigged()) return null;
    if (loadImageButton == null) {
      loadImageButton = new JButton("Load");
      loadImageButton.addActionListener(new LoadImageButtonActionListener());
    }
    return loadImageButton;
  }
  
  private boolean loadImageButtonConfigged() {
    return fieldXmlBean!=null && fieldXmlBean.xgetEnableLoadImage()!=null
      && fieldXmlBean.getEnableLoadImage();
  }
  
  private class LoadImageButtonActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        log().debug("Loading image " + getText());
        try {
            ImageManager.inst().loadAndShowImage(getText());
        } catch (IOException io) {
            JOptionPane.showMessageDialog(null,io.getMessage(),"Couldn't load image",
                                          JOptionPane.ERROR_MESSAGE);
        }
    }
  }

  JButton getBrowseImageButton() { 
    if (!loadImageButtonConfigged()) return null;
    if (browseImageButton == null) {
      browseImageButton = new JButton("Browse");
      browseImageButton.addActionListener(new BrowseImageButtonActionListener());
    }
    return browseImageButton;
  }
  
  private class BrowseImageButtonActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        String selectedImage = ImageManager.inst().chooseImage();
        if (selectedImage == null)
            return;
        setText(selectedImage);
        log().debug("Loading chosen image " + getText());
        try {
            ImageManager.inst().loadAndShowImage(getText());
            getEditManager().updateModel(getSelectedChars(), getCharField(), selectedImage, this);
        } catch (IOException io) {
            JOptionPane.showMessageDialog(null,io.getMessage(),"Couldn't load image " + getText(),
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
      // Everything in this "if" is now commented out
//      if (e.getSource() != CharFieldGui.this && e.isUpdateForCharField(charField)) {
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
//      }
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

//   public static class CharFieldGuiEx extends Exception {
//     protected CharFieldGuiEx(String m) { super(m); }
//   }

  /** overridden by term comp list */
  OBOClass getCurrentOboClass() throws CharFieldGuiEx {
    throw new CharFieldGuiEx("Field has no OBO Class");
  }

  /** overridden by RelationCompList */
  public OBOProperty getCurrentRelation() throws CharFieldGuiEx {
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

  boolean hasEditButton() { return getEditButton() != null; }
  JButton getEditButton() {
    if (!editButtonConfigged()) return null;
    if (editButton == null) {
      editButton = new JButton("Edit");
      editButton.addActionListener(new EditButtonActionListener());
    }
    return editButton;
  }
  
  private boolean editButtonConfigged() {
    return fieldXmlBean!=null && fieldXmlBean.xgetEnableBigTextBox()!=null
      && fieldXmlBean.getEnableBigTextBox();
  }
  
  /** launch big text field window when edit button pressed */
  private class EditButtonActionListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      // we should have a getParentFrame method for app
      Frame frame = (Frame)(CharFieldGui.this.editButton.getTopLevelAncestor());
      new BigTextPopup(frame,getCharField(),getSelectedChars());
    }
  }


  /** should get this from config... stub for now */
  protected boolean hasListGui() {
    return charField.isList() && !charField.isCompound();
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
      // enough for 3 rows with no scroll (karen request) 54 pixels
      listScroll.setMinimumSize(new Dimension(180,54)); // w,h
      listScroll.setPreferredSize(new Dimension(230,54)); // 130,60 small?
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

  /** update/synch jlist gui for list from char change/edit or selection */
  private void updateListGui() {
    if (!hasListGui()) return;
    setListMessage("");
    List<CharacterI> sel = getSelectedChars();
    // NO SELECTION
    if (sel==null || sel.size()==0) {
      valueListModel.clear();
      return;
    }
    // MULTI SELECT
    // clear? show intersection? union? first? message?
    // if same show list
    else if (sel.size() > 1) {
      //return; // set to multiple values, todo: check if lists are the same
      CharFieldValue first = sel.get(0).getValue(getCharField());
      List<CharFieldValue> intersection =
        first.cloneCharFieldValue().getCharFieldValueList();
      boolean notSameLists = false;
      for (int i=1; i<sel.size(); i++) { // skip 1st
        CharFieldValue cfvParent = sel.get(i).getValue(getCharField());
        // retainAll does intersection, returns true if changed
        List<CharFieldValue> list = cfvParent.getCharFieldValueList();
        notSameLists |= intersection.retainAll(list);
        // retainAll doesnt catch case where list has vals inter doesnt
        notSameLists |= intersection.size() != list.size();
//         if (!cfvList.equals(first)) {
//           sameLists = false;
//           // intersection - remove kids not in both lists!
//           for (CharFieldValue kid : intersection) {
//             // can we remove from intersection while iterating on it?
//             if (!cfvList.hasKid(kid)) intersection.remove(kid);
//           }
//         }
      }
      getValueListModel().setList(intersection);
      if (notSameLists)
        setListMessage("Multi select list differs. Showing common terms ");
    }
    // SELECT SINGLE
    else {
      CharacterI c = sel.get(0); // sole selection
      List<CharFieldValue> vals = c.getValue(getCharField()).getCharFieldValueList();
      getValueListModel().setList(vals);
    }
  }

  /** list message is to display message to user on multi select that not all 
      lists selected are same - if so */
  JLabel getListMessage() {
    if (listMessage==null) {
      listMessage = new JLabel();
      listMessage.setForeground(Color.RED);
    }
    return listMessage;
  }

  private void setListMessage(String m) {
    getListMessage().setText(m);
  }

  protected boolean alreadyInList(OBOObject obj) {
    if (!hasListGui()) return false;
    return getValueListModel().hasObject(obj);
  }
  
  /** For fields with lists - this is the model for JList, manages a list of 
      char field values */
  @SuppressWarnings("serial")
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
      // need to be able to delete from multi select
      //if (sel.size() > 1) log().error("Cant delete list items in multi select");
      CharFieldValue old = charValueList.get(i);
      // delete value in old from all selected characters
      getEditManager().deleteFromValList(this,old,sel);
    }
    /** return true if one of char field values has obj */
    private boolean hasObject(OBOObject obj) {
      for (CharFieldValue v : charValueList) {
        if (v.hasObject(obj)) return true;
      }
      return false;
    }
  }
  
  
  /** no op - override in term completion gui */
  public void setMinCompChars(int minCompChars) {}
  public int getMinCompChars() { return 0; }
  
  public void addActionListener(ActionListener l) {
    this.actionListeners.add(l);
  }
  
  public void removeActionListener(ActionListener l) {
    this.actionListeners.remove(l);
  }
  
  protected void fireActionPerformed() {
    final ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "valueChanged");
    for (ActionListener listener : this.actionListeners) {
      listener.actionPerformed(event);
    }
  }

  private static Logger log() {
    return Logger.getLogger(CharFieldGui.class);
  }
}


// GARBAGE
//   private RelationCompList getRelComp() {
//     //return relCompList;
//     return null; // overridden
//   }
