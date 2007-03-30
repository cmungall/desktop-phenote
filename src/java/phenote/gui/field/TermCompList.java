package phenote.gui.field;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;
//import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyException;
import phenote.datamodel.OntologyManager;
import phenote.edit.EditManager;
import phenote.edit.CompoundTransaction;
//import phenote.edit.UpdateTransaction;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.UseTermEvent;
import phenote.gui.selection.UseTermListener;

class TermCompList extends AbstractAutoCompList {

  private OBOClass currentOboClass=null;
  // only term comp lists need ontology choosers - if that changes move to AACL
  private JComboBox ontologyChooserCombo;
  private CharFieldGui charFieldGui;
  private static final String ALL = "ALL";
  private JButton postCompButton;


  protected TermCompList(CharField cf) {
    super(cf);
    init();
  }

//   protected TermCompList(CharField cf, boolean addCompButton) {
//     super(cf);
//     this.addCompButton = addCompButton;
//     init();
//   }

  // pase?
//   protected TermCompList(CharField cf, SearchParamsI sp, boolean editModel) {
//     this(cf,sp,editModel,true,null); // true - dont supress configured comp button
// //     super(sp,editModel,cf);  protected CharFieldGui(CharField charField,String label) {
//     init(charField,label);
//   }

// //     enableTermInfoListening();
// //     if (hasMoreThanOneOntology()) // super AACL
// //       initOntologyChooser(getCharField());
// //     if (charField.postCompAllowed() && addCompButton) {
// //       JButton postCompButton = new JButton("Comp"); // ???
// //       postCompButton.addActionListener(new PostCompListener());
// //       //fieldPanel.addPostCompButton(postCompButton);
// //     } 
//   }

//   protected TermCompList(CharField cf, SearchParamsI sp, boolean editModel,
//                          boolean addCompButton, String label) {
//     super(sp,editModel,cf,label);
//     init();
//   }
  
  private void init() {
    enableTermInfoListening();
    if (hasMoreThanOneOntology()) // super AACL
      initOntologyChooser(getCharField());
//     if (charField.postCompAllowed() && addCompButton) {
//       JButton postCompButton = new JButton("Comp"); // ???
//       postCompButton.addActionListener(new PostCompListener());
//       //fieldPanel.addPostCompButton(postCompButton);
//     } 
  }


  // pase i think
//   TermCompList(CompListSearcher s,boolean editModel,CharFieldGui cfg) {
//     super(s,editModel,cfg.getCharField());
//     this.charFieldGui = cfg; // pass to super? AACL subclass CFG?
//     enableTermInfoListening();
//     if (hasMoreThanOneOntology()) // super AACL
//       initOntologyChooser(getCharField());
//   }

  protected boolean isTermCompList() { return true; }
  protected TermCompList getTermComp() { return this; } // need this?


  protected boolean hasCompButton() { return postCompButton != null; }

  protected JButton getCompButton() { return postCompButton; }

  protected void allowPostCompButton(boolean allow) {
    if (!allow) return;
    if (getCharField().postCompAllowed()) {
      postCompButton = new JButton("Comp"); // ???
      postCompButton.addActionListener(new PostCompListener());
      //fieldPanel.addPostCompButton(postCompButton);
    } 
  }

  /** char in table changed - setCurrentOboClass & text */
  protected void setValueFromChar(CharacterI chr) {
    if (chr == null) {
      log().error("ERROR: attempt to set fields from null character"); // ex?
      return;
    }
    //System.out.println(chr+" val "+chr.getValue(getCharField()));
    if (!chr.hasValue(getCharField())) {
      currentOboClass = null; // makes getCurTermRelName ""
      setText(""); // actually calls getCurTermRelNm which is ""
      return;
    }
      
    OBOClass selCharTerm = chr.getValue(getCharField()).getOboClass();
      //getCharField().getCharFieldEnum().getValue(chr).getOboClass();
    // if null then user has made a new char or selected a char with no term
    //if (selCharTerm == null) { } else { ??? covered above??
    setOboClass(selCharTerm); // doesnt allow null
    setOntologyChooserFromTerm(selCharTerm);
    //}
  }

  void setOntologyChooserFromTerm(OBOClass term) {
    if (term==null) return;
    if (ontologyChooserCombo==null) return;
    try {
      Ontology o = OntologyManager.inst().getOntologyForTerm(term);
      ontologyChooserCombo.setSelectedItem(o.getName());
    }
    catch (OntologyException e) {
      // this happens at the moment for post comp terms - need to pick out 
      // genus for ontolo chooser or add to obo session?
      //log().error("No ontology found for term "+term); // shouldnt happen
    }
  }

  protected List<CompletionTerm> getSearchItems(String input) {
    return getCompListSearcher().getStringMatchTermList(input);
  }

  /** The user has selected a term from the list, validate and set current obo class
      if doesnt validate throw ex */
  protected void setCurrentValidItem() throws OboException {
    setOboClass(getSelectedOboClass()); //this will set text to oboclass
    // send out selection event that is NOT a mouse over event (for DAG view)
    SelectionManager.inst().selectTerm(this, getSelectedOboClass(), false);
  }

  protected String getCurrentTermRelName() {
    if (currentOboClass!= null)
      return currentOboClass.getName();
    else
      return "";
  }

  /** rename setTerm? */
  protected void setOboClass(OBOClass term) {
    // actually i think null is valid for non-required fields - undo & blanking field
    // right even if required field should still be able to undo back to init/null
//     if (term == null) {
//       log().error("Attempt to set term to null");
//       return; // debug stack trace?
//     }
    currentOboClass = term;
    String val = term == null ? "" : term.getName();
    setText(val,false); // no completion
  }

  /** Throws exception if there isnt a current obo class, if the user
      has typed something that isnt yet a term - hasnt selected a term */
  OBOClass getCurrentOboClass() throws CharFieldGuiEx {
    if (currentOboClass == null) throw new CharFieldGuiEx("term is null");
    if (!currentOboClass.getName().equals(getText()))
      throw new CharFieldGuiEx("(obo class "+currentOboClass+" and input "+getText()+
                          " dont match)");
    return currentOboClass;
  }
  
  /** This gets obo class selected in completion list - not from text box 
      Returns null if nothing selected - can happen amidst changing selection 
      also used by PostCompGui 
      this doesnt necasarily stay current with user input hmmm....
      throws OboException if dont have valid term */
  private OBOClass getSelectedOboClass() throws OboException {
    Object obj = getSelectedObject(); // throws oboex
    //return oboClassDowncast(obj); // throws oboex
    CompletionTerm t = completionTermDowncast(obj);
    return t.getOboClass();
  }


  private CompletionTerm completionTermDowncast(Object obj) throws OboException {
    if (obj == null) throw new OboException();
    if ( ! (obj instanceof CompletionTerm)) {
      //log.info("Item in completion list not obo class "+obj.getClass());
      throw new OboException("Item in completion list not obo class "+obj.getClass());
    }
    return (CompletionTerm)obj;
  }


  /** edits one or more selected chars */
  protected void editModel() {
    OBOClass oboClass;
    try { oboClass = getCurrentOboClass(); }
    catch (Exception e) { return; } // shouldnt happen, error?
    if (getCharField() == null)  return; // shouldnt happen
    List<CharacterI> chars = getSelectedChars(); // from selectionManager
    //CharFieldEnum cfe = getCharField().getCharFieldEnum();
    // isDifferentia boolean?
    //CompoundTransaction ct = new CompoundTransaction(chars,cfe,oboClass);
    CompoundTransaction ct = CompoundTransaction.makeUpdate(chars,getCharField(),oboClass);
    EditManager.inst().updateModel(this,ct);
  }

  private SelectionManager getSelectionManager() {
    return SelectionManager.inst();
  }

  private List<CharacterI> getSelectedChars() {
    return getSelectionManager().getSelectedChars();
  }

  /** This is touchy stuff - so i want to be able to display info about term in 
      TermInfo when user mouses over terms in combo boxes JList. This is not
      explicitly supported by JComboBox. have to dig into its UI to get JList.
      The combo box ui selects items in JList on mouse over, this listener
      will listen for those mouse over selections 
      should this be done with a selection event - or is that
      overkill, i guess the question will anyone besides term info
      ever care about these mouse over selection - if so make generic */
//  void addCompletionListListener(ListSelectionListener lsl) {
//     if (!canGetUIJList()) return;
//     getUIJList().addListSelectionListener(lsl);  }
  //private CompletionListListener compListListener = new CompletionListListener();
  void enableTermInfoListening() { //boolean enable) {
    if (!canGetUIJList())
      return;
    //if (enable)
    getUIJList().addListSelectionListener(new CompletionListListener());
    //else getUIJList().removeListSelectionListener(compListListener);
  }

  /** this is for MOUSE OVER TERM INFO - changes selection - this is particular
   to TermCompList (for now at least) as Rels dont do term info at all - no need */
  private class CompletionListListener implements ListSelectionListener {
    public void valueChanged(ListSelectionEvent e) {
      Object source = e.getSource();
      // hate to cast but it is handy here... and it is in fact a JList
      if (!(source instanceof JList)) {
        log().error("source of combo box mouse over event is not JList "
                    +source.getClass());
        return;
      }
      JList jList = (JList)source;
      Object selectedValue = jList.getSelectedValue();
      if (selectedValue == null)
        return;
      //System.out.println("sel val "+selectedValue.getClass()+" name "+selectedValue);
      // the selected item should be an OBOClass
      if (!(selectedValue instanceof CompletionTerm)) {
        log().debug("selected completion term is not CompTerm "+selectedValue.getClass());
        return;
      }
      CompletionTerm ct = (CompletionTerm)selectedValue;
      OBOClass oboClass = ct.getOboClass();
      Object src = TermCompList.this;
      getSelectionManager().selectMouseOverTerm(src,oboClass,getUseTermListener());
      //setTextFromOboClass(oboClass);
    }
  } // end of CompletionListListener inner class
  
  private UseTermListener useTermListener;
  private UseTermListener getUseTermListener() {
    if (useTermListener == null) useTermListener = new ComboUseTermListener();
    return useTermListener;
  }

  /** Listens for UseTermEvents from term info,if editModel is true then edits model*/
  private class ComboUseTermListener implements UseTermListener {
    public void useTerm(UseTermEvent e) {
      setOboClass(e.getTerm());
      if (editModelEnabled()) editModel();
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

  protected boolean hasOntologyChooser() { return ontologyChooserCombo != null; }
  protected JComboBox getOntologyChooser() { return ontologyChooserCombo; }


  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
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
      try {
        //Ontology o = OntologyManager.inst().getOntologyForName(s);
        Ontology o = getCharField().getOntologyForName(s);
        getCompListSearcher().setOntology(o);
      }
      catch (OntologyException ex) {
        log().error(ex.getMessage());
        return;
      }
    }
    private boolean isAll(String s) { return s.equals(ALL); }
  }
  
  /** I think post-comp should only be closeable if its empty (in expand collapse
   inframe case - now window) */
  private class PostCompListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      new PostCompGui(getCharField(),getSearchParams());
    }
  }
  

}
