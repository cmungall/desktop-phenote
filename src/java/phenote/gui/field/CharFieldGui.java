package phenote.gui.field;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Container;
import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

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
import phenote.dataadapter.DataAdapterEx;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
//import phenote.edit.CompoundTransaction;
import phenote.edit.EditManager;
//import phenote.edit.UpdateTransaction;
import phenote.dataadapter.QueryableDataAdapterI;
import phenote.config.Config;
import phenote.gui.selection.CharSelectionListener;
import phenote.gui.selection.CharSelectionEvent;
import phenote.gui.selection.SelectionManager;

/** fields can either be text fields for free text or combo boxes if have 
    ontology to browse - CharFieldGui does either - with get/setText - hides the
    details of the gui - just a field that gives text 
    should there be subclasses for free text, term, & relations? hmmmm */
abstract class CharFieldGui {
  //private AutoComboBox comboBox;
  //private AbstractAutoCompList comboBox; // ???
  //private RelationCompList relCompList;
  //private TermCompList termCompList;
  //private CompListSearcher compListSearcher;
  //private JTextField textField;
  //private FreeTextField freeTextField;
  //private boolean isCompList = false;
  private CharField charField;
  //private FieldPanel fieldPanel;
  //private JComboBox ontologyChooserCombo;
  private String label;
  //private boolean enableListeners = true;
  private boolean addCompButton = true;
  /** if true then set gui but not model, for clearing on multi, default false */
  private boolean updateGuiOnly = false;
  private JButton retrieveButton;
  static int fieldHeight = 17;
  static Dimension inputSize = new Dimension(390,fieldHeight); // size of user input box
  private boolean editModel = true;
  

  /** CharFieldGui for main window not post comp box - factory method */
  static CharFieldGui makeCharFieldGui(CharField charField,SearchParamsI sp) {
    if (charField.hasOntologies()) {
      //return new TermCompList(charField,sp,true); // enable listeners
      TermCompList t = new TermCompList(charField);
      t.setSearchParams(sp);
      t.enableListeners(true);
      t.allowPostCompButton(true);
      return t;
      //return createCompList(charField,sp);
//       if (charField.isRelationship()) {
//         return new RelationCompList(searcher,editModel,charField);
//       }
//       else {
//         return new TermCompList(searcher,editModel,charField);
//       }
    }
    else {
      FreeTextField f = new FreeTextField(charField);
      f.enableListeners(true);
      return f;
    }
  }

//   private static CharFieldGui createCompList(CharField charField, SearchParamsI sp) {
//     // enableListeners - if false then ACB wont directly edit model (post comp)
//     //compListSearcher = new CompListSearcher(charField.getOntologyList(),sp);
//       //new CompListSearcher(charField.getFirstOntology(),fieldPanel.getSearchParams());
//     if (charField.isRelationshipList()) {
//       return new RelationCompList(sp,enableListeners,charField);
//     }
//     else {
//       return new TermCompList(sp,enableListeners);
//     }
//   }

  // private static CharFieldGui createFreeTextField() {} ??

  /** createPostCompRelationList - will relation lists ever be in main window and if
      so will they ever have listeners enabled - maybe, probably not */
  static CharFieldGui makeRelationList(CharField cf,SearchParamsI sp) {
    RelationCompList r = new RelationCompList(cf);
    r.setSearchParams(sp); // does rel really need search params?
    return r;
  }

  /** make term completion lists for post comp window (genus & diff), they dont listen
      to selection nor edit model - isolated */
  static CharFieldGui makePostCompTermList(CharField cf,SearchParamsI sp,
                                                   String label) {
    // false - no listeners(dont edit model), false - dont add comp button
    // eventually adding comp button come from config for recursive comp
    //boolean allowPostCompBut = false;
    TermCompList t =  new TermCompList(cf);
    t.setSearchParams(sp);
    // t.isInSeparateWindow(true) or t.isolate(true)??
    t.enableEditModel(false);
    t.enableListeners(false);
    t.allowPostCompButton(false); // eventually config for recurse/embed
    t.setLabel(label);
    return t;
  }



  protected CharFieldGui(CharField charField) {
    init(charField);
  }
//   protected CharFieldGui(CharField charField,String label) {
//     init(charField,label);
//   }

//   protected CharFieldGui(CharField charField,boolean enableListeners) {
//     //this.enableListeners = enableListeners;
//     enableListeners(enableListeners);
//     init(charField);
//   }
    

//   CharFieldGui(CharField charField, FieldPanel tp) {/*Container parent,*/
//     init(charField,tp);
//   }

//   /** @param enableListeners - a catchall flag for disabling editing model, listening
//       to model edits, & litening to selection - postcompGui handles these and sets
//       this false, in main window this is true - rename? more flags? subclass?
//       postCompGui mode?
//       @param addCompButton if false override configuration and dont show
//       post comp button */
//   CharFieldGui(CharField cf,FieldPanel tp,String label,boolean enableListeners,
//                boolean addCompButton) {
//     this.label = label;
//     this.enableListeners = enableListeners;
//     this.addCompButton = addCompButton; // post comp button
//     init(cf,tp);
//   }


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
  protected abstract Component getUserInputGui();

  protected void enableListeners(boolean enable) {
    if (!enable) return;

    // listens for selection (eg from table) - not for PostCompGui
    //if (enableListeners)
    SelectionManager.inst().addCharSelectionListener(new FieldCharSelectListener());
    // listen for model changes (eg TermInfo commit)
    
    // this needs renaming. if not editing model then its a post comp window, and 
    // post comp windows also listen for char changes in PostCompGui not in there
    // char field guis. either add a new param or rename "enableListeners" - hmmm
    // or subclass CharFieldGui?? hmmmmm.... oh right this is changes in model
    // from the main window i think??
    //if (enableListeners)
    EditManager.inst().addCharChangeListener(new FieldCharChangeListener());
  }

  protected void enableEditModel(boolean em) { editModel = em; }
  protected boolean editModelEnabled() { return editModel; }
  
  // overridden by AbstaractAutoCompList
  protected void setSearchParams(SearchParamsI sp) {}
  

  /** no-op - overridden by term comp list - set to false for now for terms in comp
      window - resursion coming... */
  protected void allowPostCompButton(boolean allow) {}

  private void addRetrieveButton() {
    if (!Config.inst().hasQueryableDataAdapter()) return;

    QueryableDataAdapterI qa = Config.inst().getQueryableDataAdapter(); // for now just one
    if (qa.isFieldQueryable(getCharField().getName())) {
      retrieveButton = new JButton("Retrieve");
      retrieveButton.addActionListener(new RetrieveActionListener(qa));
      //fieldPanel.addRetrieveButton(b);
    }
  }

  boolean hasRetrieveButton() { return retrieveButton != null; }
  JButton getRetrieveButton() { return retrieveButton; }

  private class RetrieveActionListener implements ActionListener {
    QueryableDataAdapterI qda;
    private RetrieveActionListener(QueryableDataAdapterI q) { qda = q; }
    public void actionPerformed(ActionEvent e) {
      try {
        String queryValue = getText();
        if (isTermCompList()) { queryValue = getCurrentOboClass().getID(); }
        CharacterListI cl = qda.query(charField.getName(),queryValue);
        //if (cl == null) if null shouldve thrown ex - check anyways?
        //notifyNewCharList(cl);
        // check if unsaved data - if so ask user if wants to save/load
        CharacterListManager.inst().setCharacterList(this,cl);
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
        setValueFromChar(getFirstSelectedChar());
        //updateGuiOnly = false; // reenable model editing
      }
    }
  }

  /** for testing and internal use */
  AbstractAutoCompList getCompList() { return null; } // overridden
//     if (!isCompList())
//       return null;
//     if (isRelationshipList())
//       return getRelComp();
//     return getTermComp();
//     //return comboBox;
//   }

  // hasOntology? overridded by AbstractAutoCompList
  boolean isCompList() { return false; }// isCompList; }

  private boolean isRelationshipList() {
    return charField.isRelationship();
  }

  protected TermCompList getTermComp() {
    // throw ex if null?
    //return termCompList;
    return null; // overridden by TermCompList
  }

//   private RelationCompList getRelComp() {
//     //return relCompList;
//     return null; // overridden
//   }

  // hardwired in term & rel subclasses now
//   void enableTermInfoListening(boolean enable) {
//     if (!isCompList()) return;
//     getCompList().enableTermInfoListening(enable);
//   }

  /** Set the gui from the model (selection) */
  void setValueFromChar(CharacterI character) {
    // call this check from subclass? no do this check in subclass!
//     if (character == null) {
//       log().error("ERROR: attempt to set fields from null character"); // ex?
//       return;
//     }
//     if (charField == null) return;
//     if (charField.getCharFieldEnum() == null) {
//       log().error("Cant set value for field "+getLabel()+". Gui for character field has not "
//                   +"been associated with a datamodel field. check field names in config");
//       return;
//     }
    // if its a comp list need to set its model/current term rel (for AACL.setText)
    /// overridden in subclass
    //if (isCompList()) getCompList().setValueFromChar(character);
    //else getFreeTextField().setValueFromChar(character);
      
    //String v = charField.getCharFieldEnum().getValue(character).getName();
    //setText(v); ??
  }


  protected void setLabel(String label) {
    this.label = label;
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

    // POST COMPOSITION button - only get post comp button if both configged for it
    // AND addCompButton flag is true - PostCompGui sets to false - no post comp of 
    // post comp yet - and if so probably would do in same window not multiple
    // windows anyways
    // this belongs in TermCompList - free text doesnt use
//     if (charField.postCompAllowed() && addCompButton) {
//       JButton postCompButton = new JButton("Comp"); // ???
//       postCompButton.addActionListener(new PostCompListener());
//       fieldPanel.addPostCompButton(postCompButton);
      // keep this here for now - may reinstate - this is inpanel post comp
//       // todo -> get pc differentia ontologies from config, >1 add chooser
//       AutoComboBox postCompCombo =
//         new AutoComboBox(charField.getFirstOntology(),fieldPanel.getSearchParams());
//       // postCompCombo.setVisible(false); // initial state hidden - layout?
//       postCompCombo.setCharField(harField);
//       postCompCombo.setIsDifferentia(true); // differentia of genus in post comp
//       fieldPanel.addFieldGui(postCompCombo);
//     }
//   }

//   /** sets up rel or term comp list depending on isRelList(). also set up 
//       compListSearcher */
//   private void createCompList() {
//     //comboBox = new AutoComboBox(charField.getFirstOntology(),
//     //fieldPanel.getSearchParams(),enableListeners);
//     // enableListeners - if false then ACB wont directly edit model (post comp)
//     compListSearcher =
//       new CompListSearcher(charField.getOntologyList(),fieldPanel.getSearchParams());
//       //new CompListSearcher(charField.getFirstOntology(),fieldPanel.getSearchParams());
//     if (isRelationshipList()) {
//       relCompList = new RelationCompList(compListSearcher,enableListeners,charField);
//     }
//     else {
//       termCompList = new TermCompList(compListSearcher,enableListeners,this);
//     }
//   }


//  void addOntologyChooser(JComboBox oc) { fieldPanel.addOntologyChooser(oc); }

//   private void initOntologyChooser(CharField charField) {
//     ontologyChooserCombo = new JComboBox();
//     // add listener....
//     for (Ontology o : charField.getOntologyList()) {
//       ontologyChooserCombo.addItem(o.getName());
//     }
//     ontologyChooserCombo.addActionListener(new OntologyChooserListener());
//     fieldPanel.addOntologyChooser(ontologyChooserCombo);
//   }

//   private void initTextField(String label) {
//     //isCompList = false;
//     fieldPanel.addLabel(label);
//     freeTextField = new FreeTextField(this);
// //     textField = new JTextField(25);
// //     textField.setEditable(true);
// //     textField.getDocument().addDocumentListener(new TextFieldDocumentListener());
// //     textField.addKeyListener(new TextKeyListener());
//     fieldPanel.addFieldGui(freeTextField.getComponent());
//   }

//  private FreeTextField getFreeTextField() { return freeTextField; }

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


  protected abstract void setText(String text);// {
    // set/getText interface to combo & text field?
    //if (isCompList()) getCompList().setText(text);
    //else getFreeTextField().setText(text);}
  protected abstract String getText(); // {
    //if (isCompList()) return getCompList().getText();
    //else return getFreeTextField().getText();}
  /** clears gui not model - for multi select - may want to set to * or something? */
  protected abstract void setGuiForMultiSelect(); // {
//     if (isCompList()) { getCompList().setGuiForMultiSelect(); }
//     else {
//       updateGuiOnly = true;
//       setText("*"); // or * for multi sel? rename method setGuiForMultiSelect?
//       updateGuiOnly = false;   } }

  protected void setOboClass(OBOClass term) {
    // no-op overridden by TermCompList
    //if (!isCompList() || isRelationshipList()) return; // throw ex?? 
    //getTermComp().setOboClass(term);
    //else textField.setText(term.getName()); // shouldnt happen
  }

  /** for auto combos (ontol) for relationships (post comp rel)
   overriddedn by RelationCompList */
  void setRel(OBOProperty rel) {}
//     if (!isCompList() || !isRelationshipList()) return; // ex???
//     getRelComp().setRel(rel);
//     //else textField.setText(rel.getName()); // shouldnt actually happen
//   }

  CharFieldEnum getCharFieldEnum() { return charField.getCharFieldEnum(); }
  protected CharField getCharField() { return charField; }

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


//   /** I think post-comp should only be closeable if its empty (in expand collapse
//    inframe case - now window) */ --> TermCompList
//   private class PostCompListener implements ActionListener {
//     public void actionPerformed(ActionEvent e) {
//       new PostCompGui(charField,fieldPanel.getSearchParams());
//     }
//   }

  static class CharFieldGuiEx extends Exception {
    protected CharFieldGuiEx(String m) { super(m); }
  }

  /** should define a CharFieldGuiEx! */
  OBOClass getCurrentOboClass() throws CharFieldGuiEx {
    //if (!isCompList() || isRelationshipList)
    //if (!isTermCompList())
    // overridden by term comp list
    throw new CharFieldGuiEx("Field has no OBO Class");
      //return null; 
    //return getTermComp().getCurrentOboClass(); // throws Ex
  }

  /** overridden by RelationCompList */
  OBOProperty getCurrentRelation() throws CharFieldGuiEx {
    //if (!isCompList() || !isRelationshipList())
    throw new CharFieldGuiEx("Field has no Relation");
    //return getRelComp().getCurrentRelation(); // throws Ex
  }

  protected boolean isTermCompList() {
    return false; // overridden by term comp list
    //return isCompList() && !isRelationshipList() && getTermComp()!=null;
    // && getTermComp != null?
  }

  /** for post comp gui to set ontol chooser */
  void setOntologyChooserFromTerm(OBOClass term) {
    //if (!isTermCompList()) return; // shouldnt happen - ex?
    // overridden by term comp
    //getTermComp().setOntologyChooserFromTerm(term);
  }
    
  protected boolean hasOntologyChooser() { return false; }
  protected JComboBox getOntologyChooser() { return null; }

  /** Overridden by TermCompList */
  protected boolean hasCompButton() { return false; }
  /** Overridden by TermCompList */
  protected JButton getCompButton() { return null; }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
