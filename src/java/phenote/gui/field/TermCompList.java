package phenote.gui.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.obo.datamodel.LinkedObject;
import org.obo.datamodel.OBOClass;
import org.obo.util.TermUtil;
import org.oboedit.gui.Selection;
import org.oboedit.gui.SelectionTransferHandler;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.edit.CompoundTransaction;
import phenote.gui.DelegatingTransferHandler;
import phenote.gui.SearchParams;
import phenote.gui.selection.UseTermEvent;
import phenote.gui.selection.UseTermListener;

public class TermCompList extends AbstractAutoCompList {

  private OBOClass currentOboClass = null; // --> AnnotatedObj? OBOObject?
  // only term comp lists need ontology choosers - if that changes move to AACL
  private JComboBox ontologyChooserCombo;
  private static final String ALL = "ALL";
  private JButton postCompButton;


  public TermCompList(CharField cf,int minCompChars) {
    // AbstractAutoCompList constructor - does pre-populating if minCompChars==0
    super(cf,minCompChars);
    init();
  }


  private void init() {
    enableTermInfoListening();
    if (hasMoreThanOneOntology()) // super AACL
      initOntologyChooser(getCharField());
    // if config.showAllOnEmptyInput...
    this.configureTransferHandler();
  }


  protected boolean isTermCompList() {
    return true;
  }

  protected TermCompList getTermComp() {
    return this;
  } // need this?


  protected boolean hasCompButton() {
    return postCompButton != null;
  }

  protected JButton getCompButton() {
    return postCompButton;
  }

  protected void checkPostCompButton(boolean allow) {
    if (!allow) return;
    if (getCharField().postCompAllowed()) {
       postCompButton = new JButton("Compose"); 
       //aabdulla: renaming comp to compose suggesting composition of ontology terms with relationships.
      postCompButton.addActionListener(new PostCompListener());
      //fieldPanel.addPostCompButton(postCompButton);
    }
  }
  
  protected void setCharFieldValue(CharFieldValue value) {
    this.setOboClass(value.getOboClass());
    this.setOntologyChooserFromTerm(value.getOboClass());
  }
  
  protected CharFieldValue getCharFieldValue() {
    try {
      return this.getCharField().makeValue(null, this.getCurrentOboClass().getID(), this.getText());
    } catch (CharFieldException e) {
      log().debug("Couldn't create charfieldvalue", e);
    } catch (CharFieldGuiEx e) {
      log().debug("Couldn't create charfieldvalue", e);
    }
    return CharFieldValue.emptyValue(null, this.getCharField());
  }

  void setOntologyChooserFromTerm(OBOClass term) {
    if (term == null) return;
    if (ontologyChooserCombo == null) return;
    try {
      Ontology o = CharFieldManager.inst().getOntologyForTerm(term);
      ontologyChooserCombo.setSelectedItem(o.getName());
    }
    catch (OntologyException e) {
      // this happens at the moment for post comp terms - need to pick out 
      // genus for ontolo chooser or add to obo session?
      //log().error("No ontology found for term "+term); // shouldnt happen
    }
  }

  /** now threaded so cant return - when done sets
      AbstractAutoCompList.ListSearchListener.newResults */
  protected void getSearchItems(String input,SearchListener l,boolean thread) {
    /*return*/ getCompListSearcher().getStringMatchTermList(input,l,thread);
  }

  /**
   * The user has selected a term from the list, validate and set current obo class
   * if doesnt validate throw ex
   called by updateModel to get valid term.
   if useTopHit is true then input is partial string and use top hit in
   comp list. if false only use selected term - throw oboex otherwise
   */
  protected void setCurrentValidItem(boolean useTopHit) throws OboException {
    OBOClass inputTerm = getOboClassFromInput(useTopHit); // throws OboEx
    setOboClass(inputTerm); //this will set text to oboclass
    // send out selection event that is NOT a mouse over event (for DAG view)
    this.getSelectionManager().selectTerm(this, inputTerm, false);
  }

  protected String getCurrentTermRelName() {
    if (currentOboClass != null)
      return currentOboClass.getName();
    else
      return "";
  }

  /**
   * rename setTerm?
   */
  protected void setOboClass(OBOClass term) {
    // actually i think null is valid for non-required fields - undo & blanking field
    // right even if required field should still be able to undo back to init/null
//     if (term == null) {
//       log().error("Attempt to set term to null");
//       return; // debug stack trace?
//     }
    currentOboClass = term;
    if (TermUtil.isDangling(currentOboClass)) {
      // this doesnt work on a mac!!!
      this.setForegroundColor(Color.RED);
      //phenote.error.ErrorManager.inst().error(new phenote.error.ErrorEvent(this,"setting dangle color to red"));
    }
    else
      this.setForegroundColor(this.getEnabledTextColor());
    String val = term == null ? "" : term.getName();
    setText(val); // no completion with setText fyi
  }

  /**
   * Throws exception if there isnt a current obo class, if the user
   * has typed something that isnt yet a term - hasnt selected a term
   */
  public OBOClass getCurrentOboClass() throws CharFieldGuiEx {
    if (currentOboClass == null) throw new CharFieldGuiEx("term is null");
    if (!currentOboClass.getName().equals(getText()))
      throw new CharFieldGuiEx("(obo class " + currentOboClass + " and input " + getText() +
              " dont match)");
    return currentOboClass;
  }

  /**
   * This gets obo class selected in completion list - not from text box
   * Returns null if nothing selected - can happen amidst changing selection
   * also used by PostCompGui
   * this doesnt necasarily stay current with user input hmmm....
   * throws OboException if dont have valid term
   */
  private OBOClass getOboClassFromInput(boolean useTopHit) throws OboException {
    // CompletionTerm if selected, String if typed & return
    Object obj = getSelectedObject(); // throws oboex
    if (obj==null) {
      if (!useTopHit)
        throw new OboException("selected obj is null");
      // just get text straight from textfield, key listen event came in before
      // sel obj got set
      else 
        obj = getText(); 
    }
      
    //return oboClassDowncast(obj); // throws oboex
    CompletionObject t = getCompTermFromInput(obj,useTopHit);
    if (!t.hasOboClass()) throw new OboException("Selected obj doesnt have obo class");
    return t.getOboClass();
  }


  /** this takes object from user selection and casts it to CompletionObject, if not
      CompletionObject throws OboException. If user hits return in box obj will actually
      be a String of user input NOT CompTerm. but new request is to go with top 
      option at that point. So I think this will then go for top CompObj in
      comp list and this method needs to be renamed if so.
      I think i didnt realize previous that returns put Strings out from comp
      so if useTopHit is false and input is String not compTerm then throw OboEx
      if useTopHit is true and input is String grab top compTerm from comp list
      for user hitting return on partial string - convenience!
  */
  //private CompletionTerm completionTermDowncast(Object obj) throws OboException {
  private CompletionObject getCompTermFromInput(Object obj,boolean useTopHit)
    throws OboException {
    if (obj == null) throw new OboException();
    // STRING - user hit return
    if (!(obj instanceof CompletionObject)) {
      //log.info("Item in completion list not obo class "+obj.getClass());
      if (!useTopHit)
        throw new OboException("Item in comp list not obo class "+obj.getClass());
      // The entry is a string and its what the user has typed so far, 
      // return 1st item of comp list - should it be exact? syn? only item?
      if (!(obj instanceof String)) // i dont think this is possible
        throw new OboException("Item in comp list not obo class nor string"
                               + obj.getClass());
      String input = (String)obj;
      // Constraint: need a least 1 letter/char
      if (input == null || input.trim().length() == 0)
        throw new OboException("no input given"); // msg not used
      CompletionObject ct = getFirstCompTerm();
      // dont think this can happen - safety
      if (!ct.matches(input,SearchParams.inst()))
        throw new OboException("input & 1st term dont match");
      // compare string with ct?? probably...
      return ct; // hmmmm
    }
    // TERM - user selected term
    return (CompletionObject) obj;
  }

  /** Returns first CompletionTerm in completion list 
   throws obo exception if list is empty (or not comp term) */
  private CompletionObject getFirstCompTerm() throws OboException {
    Object o = super.getFirstCompListObj(); // throws ex if emtpy
    if (!(o instanceof CompletionObject)) throw new OboException();
    return (CompletionObject)o;
  }

  /**
   * edits one or more selected chars if there is valid input, if not sets model to null
   if useTopHit is false then only set model if have selected comp term (user selected 
   from list). if useTopHit is true, then input is user string, (user has hit enter)
   and use the top hit in the list that comes from that partial string
   */
  protected void updateModel(boolean useTopHit) {
    log().debug("update model");
    // TRY to get valid input...
    try {
      // sets obo class from selected comp term OR top term in list (if no comp term
      // selected), throws OboException if invalid term (eg no input)
      this.setCurrentValidItem(useTopHit); 
    }
    // NOT VALID input, set model to null, and return
    catch (OboException e) {
      if (!this.editModelEnabled()) {
        return;
      }
      if ((this.getText() == null) || (this.getText().equals(""))) {
        this.setModelToNull();
        return;
      } else {
        this.setValueFromChars(this.getSelectedChars());
      }
      return;
    }
    
    // We have a VALID item - go ahead and commit to datamodel
    if (this.editModelEnabled()) {
      try {
        OBOClass oboClass = getCurrentOboClass();
        log().debug("Setting model: " + oboClass);
        this.setModel(oboClass);
      }
      catch (CharFieldGuiEx e) {
        return;
      } // shouldnt happen, error?
    }
  }


  /** oc may be null - for nullifying model, though lists dont accept nulls
      as they have explicit delete */
  private void setModel(OBOClass oboClass) {
     // null for list shouldnt happen, give stack for debug and return
    if (getCharField().isList() && oboClass==null) {
      // actually this is normal, if lose focus on empty field, so just return
      // no need for error message
//       log().error("Got null term for list, shouldnt happen. Ignoring. "
//                   +"StackTrace for debug:");
//       new Throwable().printStackTrace();
      return;
    }
    if (alreadyInList(oboClass)) return; // if list, cancel if non unique
    if (this.getCharField() == null) return; // shouldn't happen
    final List<CharacterI> chars = this.getSelectedChars();
    if (chars.isEmpty()) return;
    if (this.areCharactersEqualForCharField(chars, this.getCharField())) {
      final OBOClass modelTerm = chars.get(0).getValue(this.getCharField()).getOboClass();
      final boolean equal = (modelTerm == null) ? (modelTerm == oboClass) : (modelTerm.equals(oboClass));
      if (equal) return; // don't edit model - the value is the same
    }
    final CompoundTransaction ct =
      CompoundTransaction.makeUpdate(chars, getCharField(), oboClass);
    // updateModel triggers change event, triggers table to fire valueChanged, if in list then
    // lose which part of the list is selected, but shouldnt listen to valueChanged in this 
    // case in the first place, this flag supresses valueChanged - alternative?
    setDoingInternalEdit(true);
    this.getEditManager().updateModel(this, ct);
    setDoingInternalEdit(false);
  }


  // allow user to nullify field
  protected void setModelToNull() {
    setModel(null);
  }

  /**
   * This is touchy stuff - so i want to be able to display info about term in
   * TermInfo when user mouses over terms in combo boxes JList. This is not
   * explicitly supported by JComboBox. have to dig into its UI to get JList.
   * The combo box ui selects items in JList on mouse over, this listener
   * will listen for those mouse over selections
   * should this be done with a selection event - or is that
   * overkill, i guess the question will anyone besides term info
   * ever care about these mouse over selection - if so make generic
   */
  void enableTermInfoListening() { //boolean enable) {
    if (!canGetUIJList())
      return;
    //if (enable)
    getUIJList().addListSelectionListener(new CompletionListListener());
    //else getUIJList().removeListSelectionListener(compListListener);
  }

  /**
   * this is for MOUSE OVER TERM INFO - changes selection - this is particular
   * to TermCompList (for now at least) as Rels dont do term info at all - no need
   */
  private class CompletionListListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      Object source = e.getSource();
      // hate to cast but it is handy here... and it is in fact a JList
      if (!(source instanceof JList)) {
        log().error("source of combo box mouse over event is not JList "
                + source.getClass());
        return;
      }
      JList jList = (JList) source;
      Object selectedValue = jList.getSelectedValue();
      if (selectedValue == null)
        return;
      //System.out.println("sel val "+selectedValue.getClass()+" name "+selectedValue);
      // the selected item should be an OBOClass
      if (!(selectedValue instanceof CompletionObject)) {
        log().debug("selected completion term is not CompObj " + selectedValue.getClass());
        return;
      }
      CompletionObject ct = (CompletionObject) selectedValue;
      if (!ct.hasOboClass()) { // shouldnt happen
        log().debug("selected completion term does not have OBOClass "+ct.getOboObject());
        return;
      }
      OBOClass oboClass = ct.getOboClass();
      Object src = TermCompList.this;
      getSelectionManager().selectMouseOverTerm(src, oboClass, getUseTermListener());
      //setTextFromOboClass(oboClass);
    }
  } // end of CompletionListListener inner class

  private UseTermListener useTermListener;

  private UseTermListener getUseTermListener() {
    if (useTermListener == null) useTermListener = new ComboUseTermListener();
    return useTermListener;
  }

  /**
   * Listens for UseTermEvents from term info,if editModel is true then edits model
   */
  private class ComboUseTermListener implements UseTermListener {
    public void useTerm(UseTermEvent e) {
      if (!e.isTerm()) return;
      if ((TermCompList.this.editModelEnabled()) && (TermCompList.this.getSelectedChars().isEmpty())) return;
      TermCompList.this.setOboClass(e.getTerm());
      if (TermCompList.this.editModelEnabled()) TermCompList.this.setModel(e.getTerm());
    }
  }

  private void initOntologyChooser(CharField charField) {
    ontologyChooserCombo = new JComboBox();
    // need to add in ALL
    ontologyChooserCombo.addItem(ALL);
    for (Ontology o : charField.getOntologyList()) {
      ontologyChooserCombo.addItem(o.getName());
    }
    ontologyChooserCombo.addActionListener(new OntologyChooserListener());
    //charFieldGui.addOntologyChooser(ontologyChooserCombo);
  }

  protected boolean hasOntologyChooser() {
    return ontologyChooserCombo != null;
  }

  protected JComboBox getOntologyChooser() {
    return ontologyChooserCombo;
  }
  
  private void configureTransferHandler() {
    final Component editor = this.getJComboBox().getEditor().getEditorComponent();
    if (editor instanceof JComponent) {
      final TransferHandler handler = ((JComponent)editor).getTransferHandler();
      ((JComponent)editor).setTransferHandler(new FieldTransferHandler(handler));
    } else {
      log().error("Could not set transfer handler for JComboBox");
    }
  }

  private Logger log;

  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

  /** listens for selection from ontology chooser drop down list */
  private class OntologyChooserListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String s = ontologyChooserCombo.getSelectedItem().toString();

      // ALL
      if (isAll(s)) {
        //getCompListSearcher().setSearchAll(true);
        getCompListSearcher().setOntologies(getCharField().getOntologyList());
        return;
      }

      // SINGLE ONTOLOGY
      //Ontology o = OntologyManager.inst().getOntologyForName(s);
      Ontology o = getCharField().getOntologyForName(s);
      if (o == null) {
        log.error("No Ontology with name :" + s + " found.");
        return;
      }

      getCompListSearcher().setOntology(o);
    }

    private boolean isAll(String s) {
      return s.equals(ALL);
    }
  }

  /**
   * I think post-comp should only be closeable if its empty (in expand collapse
   * inframe case - now window)
   */
  private class PostCompListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      final Frame frame = (Frame)(TermCompList.this.postCompButton.getTopLevelAncestor());
      new PostCompGui(getCharField(), TermCompList.this.getEditManager(),
                      TermCompList.this.getSelectionManager(), TermCompList.this.selectionModel, frame,getMinCompChars());
    }
  }
  
  /** Takes drops from oboedits tree graph which uses
      oboedit.gui.SelectionTransferHandler.SELECTION_FLAVOR and contains a 
      org.oboedit.gui.Selection which may have LinkedObjects/OBOClasses
      (even better would be if it had OBOObjects) */
  @SuppressWarnings("serial")
  private class FieldTransferHandler extends DelegatingTransferHandler {
        
    public FieldTransferHandler(TransferHandler parentHandler) {
      super(parentHandler);
    }

    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      if (super.canImport(comp, transferFlavors)) return true;
      for (DataFlavor flavor : transferFlavors) {
        if (flavor.equals(SelectionTransferHandler.SELECTION_FLAVOR)) {
          return true;
        }
      }
      return false;
    }

    @Override
    public boolean importData(JComponent comp, Transferable t) {
      for (DataFlavor flavor : t.getTransferDataFlavors()) {
        if (flavor.equals(SelectionTransferHandler.SELECTION_FLAVOR)) {
          try {
            final Selection selection = (Selection)(t.getTransferData(SelectionTransferHandler.SELECTION_FLAVOR));
            for (LinkedObject lo : selection.getTerms()) {
              // just use the first term
              if ((lo instanceof OBOClass) && this.isValidTerm((OBOClass)lo)) {
                setOboClass((OBOClass)lo);
                setModel((OBOClass)lo);
                return true;
              } else {
                return false;
              }
            }
          } catch (UnsupportedFlavorException e) {
            log().error("Data flavor missing: " + flavor, e);
            return false;
          } catch (IOException e) {
            log().error("Could not read data flavor: " + flavor, e);
            return false;
          }
        }
      }
      return super.importData(comp, t);
    }
    
    private boolean isValidTerm(OBOClass term) {
      for (Ontology ontology : getCharField().getOntologyList()) {
        if (ontology.getSortedTerms().contains(term)) return true;
      }
      return false;
    }
    
  }

}

//  void addCompletionListListener(ListSelectionListener lsl) {
//     if (!canGetUIJList()) return;
//     getUIJList().addListSelectionListener(lsl);  }
  //private CompletionListListener compListListener = new CompletionListListener();
    //getCharField().getCharFieldEnum().getValue(chr).getOboClass();
    // if null then user has made a new char or selected a char with no term
    //if (selCharTerm == null) { } else { ??? covered above??
//     if (charField.postCompAllowed() && addCompButton) {
//       JButton postCompButton = new JButton("Comp"); // ???
//       postCompButton.addActionListener(new PostCompListener());
//       //fieldPanel.addPostCompButton(postCompButton);
//     } 

