package phenote.gui.field;

//import java.util.List;
//import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
//import javax.swing.JTextField;
//import javax.swing.event.DocumentListener;
//import javax.swing.event.DocumentEvent;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.datamodel.OntologyManager;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
//import phenote.edit.CompoundTransaction;
import phenote.edit.EditManager;
//import phenote.edit.UpdateTransaction;
import phenote.gui.selection.CharSelectionListener;
import phenote.gui.selection.CharSelectionEvent;
import phenote.gui.selection.SelectionManager;

/** fields can either be text fields for free text or combo boxes if have 
    ontology to browse - CharFieldGui does either - with get/setText - hides the
    details of the gui - just a field that gives text 
    should there be subclasses for free text, term, & relations? hmmmm */
class CharFieldGui {
  //private AutoComboBox comboBox;
  //private AbstractAutoCompList comboBox; // ???
  private RelationCompList relCompList;
  private TermCompList termCompList;
  private CompListSearcher compListSearcher;
  //private JTextField textField;
  private FreeTextField freeTextField;
  private boolean isCompList = false;
  private CharField charField;
  private FieldPanel fieldPanel;
  //private JComboBox ontologyChooserCombo;
  private String label;
  private boolean enableListeners = true;
  private boolean addCompButton = true;
  /** if true then set gui but not model, for clearing on multi, default false */
  private boolean updateGuiOnly = false;
  static int fieldHeight = 17;
  static Dimension inputSize = new Dimension(390,fieldHeight); // size of user input box
  

  CharFieldGui(CharField charField, FieldPanel tp) {/*Container parent,*/
    init(charField,tp);
  }

  /** @param enableListeners - a catchall flag for disabling editing model, listening
      to model edits, & litening to selection - postcompGui handles these and sets
      this false, in main window this is true - rename? more flags? subclass?
      postCompGui mode?
      @param addCompButton if false override configuration and dont show
      post comp button */
  CharFieldGui(CharField cf,FieldPanel tp,String label,boolean enableListeners,
               boolean addCompButton) {
    this.label = label;
    this.enableListeners = enableListeners;
    this.addCompButton = addCompButton; // post comp button
    init(cf,tp);
  }

  private void init(CharField cf, FieldPanel tp) {
    charField = cf;
    fieldPanel = tp;
    if (!charField.hasOntologies())
      initTextField(charField.getName());
    else
      initCombo();

    // check queryableAdapter if charField is queryable
    addRetrieveButton();


    // listens for selection (eg from table) - not for PostCompGui
    if (enableListeners)
      SelectionManager.inst().addCharSelectionListener(new FieldCharSelectListener());
    // listen for model changes (eg TermInfo commit)
    
    // this needs renaming. if not editing model then its a post comp window, and 
    // post comp windows also listen for char changes in PostCompGui not in there
    // char field guis. either add a new param or rename "enableListeners" - hmmm
    // or subclass CharFieldGui?? hmmmmm.... oh right this is changes in model
    // from the main window i think??
    if (enableListeners)
      EditManager.inst().addCharChangeListener(new FieldCharChangeListener());
  }

  private void addRetrieveButton() {
    // if (Config.inst().hasQueryableDataAdapter()) {
    // QueryableAdapter qa = Config.inst().getQueryableDataAdapter(); // for now just one
    // if (qa.isCharFieldQueryable(cf)) {
    // Button b = new Button("Retrieve");
    // b.addActionListener(new RetrieveActionListener(qa));
    // fieldPanel.addRetrieveButton(b)
    
  }

  private class RetrieveActionListener implements ActionListener {
    QueryableDataAdapterI qda;
    private RetrieveActionListener(QueryableDataAdapterI q) { qda = q; }
    public void actionPerformed(ActionEvent e) {
      CharacterListI cl = qda.query(charField,getText());
      //notifyNewCharList(cl);
      // check if unsaved data - if so ask user if wants to save/load
      CharacterListManager.inst().setCharacterList(this,cl);
    }
  }

  // FreeTextField.updateModel uses this
  boolean updateGuiOnly() { return updateGuiOnly; }

  /** edits from post comp come in here i believe (term info used to but now
      thats done with UseTermEvent) */
  private class FieldCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      // check charField is this char field
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

        updateGuiOnly = true; // disable model editing - a better way to do this???
        setValueFromChar(getFirstSelectedChar());
        updateGuiOnly = false; // reenable model editing
      }
    }
  }

  /** for testing and internal use */
  AbstractAutoCompList getCompList() {
    if (!isCompList)
      return null;
    if (isRelationshipList())
      return getRelComp();
    return getTermComp();
    //return comboBox;
  }

  // hasOntology?
  boolean isCompList() { return isCompList; }

  private boolean isRelationshipList() {
    return charField.isRelationship();
  }

  private TermCompList getTermComp() {
    // throw ex if null?
    return termCompList;
  }

  private RelationCompList getRelComp() {
    return relCompList;
  }

  // hardwired in term & rel subclasses now
//   void enableTermInfoListening(boolean enable) {
//     if (!isCompList()) return;
//     getCompList().enableTermInfoListening(enable);
//   }

  /** Set the gui from the model (selection) */
  void setValueFromChar(CharacterI character) {
    if (character == null) {
      log().error("ERROR: setting to null character");
      return;
    }
    if (charField == null) return;
//     if (charField.getCharFieldEnum() == null) {
//       log().error("Cant set value for field "+getLabel()+". Gui for character field has not "
//                   +"been associated with a datamodel field. check field names in config");
//       return;
//     }
    // if its a comp list need to set its model/current term rel (for AACL.setText)
    if (isCompList)
      getCompList().setValueFromChar(character);
    else 
      getFreeTextField().setValueFromChar(character);
      
    //String v = charField.getCharFieldEnum().getValue(character).getName();
    //setText(v); ??
  }


  String getLabel() {
    if (label == null) {
      if (charField.hasMoreThanOneOntology() || !charField.hasOntologies())
        label = charField.getName();
      else
        label = charField.getFirstOntology().getName();
    }
    return label;
  }

  private void initCombo() { //, Container parent) {
    isCompList = true;

    //String name = charField.getFirstOntology().getName();JLabel label = 
    fieldPanel.addLabel(getLabel(),charField.hasMoreThanOneOntology());

    // if has more than one ontology(entity) then add ontology choose list
//     if (charField.hasMoreThanOneOntology())
//       initOntologyChooser(charField);

    createCompList(); // comp list now makes ontologyChooser (if >1 ont)
    fieldPanel.addFieldGui(getCompList());

    //getCompList().setCharField(charField);

    // POST COMPOSITION button - only get post comp button if both configged for it
    // AND addCompButton flag is true - PostCompGui sets to false - no post comp of 
    // post comp yet - and if so probably would do in same window not multiple
    // windows anyways
    if (charField.postCompAllowed() && addCompButton) {
      JButton postCompButton = new JButton("Comp"); // ???
      postCompButton.addActionListener(new PostCompListener());
      fieldPanel.addPostCompButton(postCompButton);
      // keep this here for now - may reinstate - this is inpanel post comp
//       // todo -> get pc differentia ontologies from config, >1 add chooser
//       AutoComboBox postCompCombo =
//         new AutoComboBox(charField.getFirstOntology(),fieldPanel.getSearchParams());
//       // postCompCombo.setVisible(false); // initial state hidden - layout?
//       postCompCombo.setCharField(harField);
//       postCompCombo.setIsDifferentia(true); // differentia of genus in post comp
//       fieldPanel.addFieldGui(postCompCombo);
    }
  }

  /** sets up rel or term comp list depending on isRelList(). also set up 
      compListSearcher */
  private void createCompList() {
    //comboBox = new AutoComboBox(charField.getFirstOntology(),
    //fieldPanel.getSearchParams(),enableListeners);
    // enableListeners - if false then ACB wont directly edit model (post comp)
    compListSearcher =
      new CompListSearcher(charField.getOntologyList(),fieldPanel.getSearchParams());
      //new CompListSearcher(charField.getFirstOntology(),fieldPanel.getSearchParams());
    if (isRelationshipList()) {
      relCompList = new RelationCompList(compListSearcher,enableListeners,charField);
    }
    else {
      termCompList = new TermCompList(compListSearcher,enableListeners,this);
    }
  }

  void addOntologyChooser(JComboBox oc) { fieldPanel.addOntologyChooser(oc); }

//   private void initOntologyChooser(CharField charField) {
//     ontologyChooserCombo = new JComboBox();
//     // add listener....
//     for (Ontology o : charField.getOntologyList()) {
//       ontologyChooserCombo.addItem(o.getName());
//     }
//     ontologyChooserCombo.addActionListener(new OntologyChooserListener());
//     fieldPanel.addOntologyChooser(ontologyChooserCombo);
//   }

  private void initTextField(String label) {
    isCompList = false;
    fieldPanel.addLabel(label);
    freeTextField = new FreeTextField(this);
//     textField = new JTextField(25);
//     textField.setEditable(true);
//     textField.getDocument().addDocumentListener(new TextFieldDocumentListener());
//     textField.addKeyListener(new TextKeyListener());
    fieldPanel.addFieldGui(freeTextField.getComponent());
  }

  private FreeTextField getFreeTextField() { return freeTextField; }

  // ?? was this moved somewhere else?
//   /** key listener for free text fields for Cmd-V pasting for macs */
//   private class TextKeyListener extends java.awt.event.KeyAdapter {
//     public void keyPressed(java.awt.event.KeyEvent e) {
//       // on a mac Command-V is paste. this aint so with java/metal look&feel
//       if (e.getKeyChar() == 'v' 
//           && e.getKeyModifiersText(e.getModifiers()).equals("Command")) {
//         //log().debug("got cmd V paste");
//         //System.getClipboard
//         Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
//         try {
//           Transferable t = c.getContents(null); // null?
//           Object s = t.getTransferData(DataFlavor.stringFlavor);
//           // this isnt quite right as it should just insert the text not wipe
//           // it out - but probably sufficient for now?
//           if (s != null)
//             setText(s.toString());
//         } catch (Exception ex) { System.out.println("failed paste "+ex); }
//       }
//     }
//   }


  void setText(String text) {
    // set/getText interface to combo & text field?
    if (isCompList) getCompList().setText(text);
    else getFreeTextField().setText(text);
  }
  String getText() {
    if (isCompList) return getCompList().getText();
    else return getFreeTextField().getText();
  }
  /** clears gui not model - for multi select - may want to set to * or something? */
  void setGuiForMultiSelect() {
    if (isCompList) {
      getCompList().setGuiForMultiSelect();
    }
    else {
      updateGuiOnly = true;
      setText("*"); // or * for multi sel? rename method setGuiForMultiSelect?
      updateGuiOnly = false;
    }
  }

  void setOboClass(OBOClass term) {
    if (!isCompList || isRelationshipList()) return; // throw ex?? 
    getTermComp().setOboClass(term);
    //else textField.setText(term.getName()); // shouldnt happen
  }

  /** for auto combos (ontol) for relationships (post comp rel) */
  void setRel(OBOProperty rel) {
    if (!isCompList || !isRelationshipList()) return; // ex???
    getRelComp().setRel(rel);
    //else textField.setText(rel.getName()); // shouldnt actually happen
  }

  CharFieldEnum getCharFieldEnum() { return charField.getCharFieldEnum(); }
  CharField getCharField() { return charField; }

  private CharacterI getFirstSelectedChar() {
    return SelectionManager.inst().getFirstSelectedCharacter();
  }

  private class FieldCharSelectListener implements CharSelectionListener {
    public void charactersSelected(CharSelectionEvent e) {
      // if multi select then clear out fields - alternatively could do first char
      // or only show fields that are all same? 
      if (e.isMultiSelect()) {
        setGuiForMultiSelect();
        return;
      }
      updateGuiOnly = true; // selection should not cause an edit/transaction!
      setValueFromChar(e.getChars().get(0));
      updateGuiOnly = false;
    }
  }


  /** I think post-comp should only be closeable if its empty (in expand collapse
   inframe case - now window) */
  private class PostCompListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      new PostCompGui(charField,fieldPanel.getSearchParams());
    }
  }

  /** should define a CharFieldGuiEx! */
  OBOClass getCurrentOboClass() throws Exception {
    //if (!isCompList || isRelationshipList)
    if (!isTermCompList())
      throw new Exception("Field has no OBO Class");
    return getTermComp().getCurrentOboClass(); // throws Ex
  }

  OBOProperty getCurrentRelation() throws Exception {
    if (!isCompList || !isRelationshipList())
      throw new Exception("Field has no Relation");
    return getRelComp().getCurrentRelation(); // throws Ex
  }

  private boolean isTermCompList() {
    return isCompList && !isRelationshipList() && getTermComp()!=null;
    // && getTermComp != null?
  }

  /** for post comp gui to set ontol chooser */
  void setOntologyChooserFromTerm(OBOClass term) {
    if (!isTermCompList()) return; // shouldnt happen - ex?
    getTermComp().setOntologyChooserFromTerm(term);
  }
    

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
  // -> TermCompList
//   private class OntologyChooserListener implements ActionListener {
//     public void actionPerformed(ActionEvent e) {
//       String s = ontologyChooserCombo.getSelectedItem().toString();
//       try {
//         Ontology o = OntologyManager.inst().getOntologyForName(s);
//         //getCompList().setOntology(o); // termComp?
//         compListSearcher.setOntology(o);
//       }
//       catch (OntologyException ex) {
//         log().error(ex.getMessage());
//         return;
//       }
//     }
//   }

//   List<CharacterI> getSelectedChars() {
//     return SelectionManager.inst().getSelectedChars();
//   }

//   // separate char text field class? --> FreeTextField
//   /** This is where the model gets updated (for free text fields) */
//   private class TextFieldDocumentListener implements DocumentListener {
//     //private String previousVal = null;
//     public void changedUpdate(DocumentEvent e) { updateModel(); }
//     public void insertUpdate(DocumentEvent e) { updateModel(); }
//     public void removeUpdate(DocumentEvent e) { updateModel(); }
//     private void updateModel() {
//       // if only updating gui (multi select clear) then dont update model
//       if (updateGuiOnly) return;
//       // on delete last pheno row clearing of text will trigger this
//       //if (!characterTablePanel.hasRows()) return;
//       //String genotype = lumpField.getText();
//       //characterTablePanel.setSelectedGenotype(genotype);
//       List<CharacterI> chars = getSelectedChars();
//       // i believe this isnt using oboClass as we just have string
//       // of course it isnt this is free text
//       String v = getText();
//       //UpdateTransaction ut = new UpdateTransaction(char,getCharFieldEnum(),v);
//       CompoundTransaction ct = new CompoundTransaction(chars,getCharFieldEnum(),v);
//       EditManager.inst().updateModel(CharFieldGui.this,ct);
//       //previousVal = v; // undo
//     }
//   }

