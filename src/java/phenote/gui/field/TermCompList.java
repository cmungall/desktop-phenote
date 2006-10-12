package phenote.gui.field;

import java.util.Vector;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharFieldEnum;
import phenote.edit.EditManager;
import phenote.edit.UpdateTransaction;
import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.UseTermEvent;
import phenote.gui.selection.UseTermListener;

class TermCompList extends AbstractAutoCompList {

  private OBOClass currentOboClass=null;

  TermCompList(CompListSearcher s,boolean editModel) {
    
    super(s,editModel);
    enableTermInfoListening();
    // this inner class enables retrieving of JList for mouse over
    // this will probably throw errors if non metal look & feel is used
//     setUI(new MetalListComboUI());
//     //setFont(new Font("Courier",Font.PLAIN,12));

//     setOntology(ontology);
//     searchParams = sp; // singleton access? part of ontology?
//     setEditable(true);
//     AutoTextFieldEditor autoTextFieldEditor = new AutoTextFieldEditor();
//     this.setEditor(autoTextFieldEditor);
//     setPreferredSize(new Dimension(350,22));

//     enableTermInfoListening(true); // default
//     //addCompletionListListener(compList);

//     //if (editModel) // ComboBoxActionListener edits the model
//     this.editModel = editModel;
//     addActionListener(new ComboBoxActionListener());

  }

  protected Vector getSearchItems(String input) {
    return getCompListSearcher().getStringMatchTerms(input);
  }

  /** The user has selected a term from the list, validate and set current obo class
      if doesnt validate throw ex */
  protected void setCurrentValidItem() throws OboException {
    setOboClass(getSelectedOboClass()); //this will set text to oboclass
    //currentOboClass = getSelectedOboClass();
  }

  /** rename setTerm? */
  void setOboClass(OBOClass term) {
    if (term == null) {
      log().error("Attempt to set term to null");
      return; // debug stack trace?
    }
    currentOboClass = term;
    setText(term.getName(),false); // no completion
  }

  /** Throws exception if there isnt a current obo class, if the user
      has typed something that isnt yet a term - hasnt selected a term */
  OBOClass getCurrentOboClass() throws Exception {
    if (currentOboClass == null) throw new Exception("term is null");
    if (!currentOboClass.getName().equals(getText()))
      throw new Exception("(obo class "+currentOboClass+" and input "+getText()+
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

//   // strings get in somehow - need to figure out where they are coming from
//   private OBOClass oboClassDowncast(Object obj) throws OboException {
//     if (obj == null) throw new OboException();
//     if ( ! (obj instanceof OBOClass)) {
//       //log.info("Item in completion list not obo class "+obj.getClass());
//       throw new OboException("Item in completion list not obo class "+obj.getClass());
//     }
//     return (OBOClass)obj;
//   }

  protected void editModel() {
    OBOClass oboClass;
    try { oboClass = getCurrentOboClass(); }
    catch (Exception e) { return; } // shouldnt happen, error?
    if (getCharField() == null)  return; // shouldnt happen
    CharacterI c = getSelectedCharacter(); // from selectionManager
    CharFieldEnum cfe = getCharField().getCharFieldEnum();
    // isDifferentia boolean?
    UpdateTransaction ut = new UpdateTransaction(c,cfe,oboClass);
    EditManager.inst().updateModel(this,ut);
  }

  private SelectionManager getSelectionManager() {
    return SelectionManager.inst();
  }

  private CharacterI getSelectedCharacter() {
    return getSelectionManager().getSelectedCharacter();
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
      if (!(selectedValue instanceof CompletionTerm)) {
        log.debug("selected completion term is not CompTerm "+selectedValue.getClass());
        return;
      }
      CompletionTerm ct = (CompletionTerm)selectedValue;
      OBOClass oboClass = ct.getOboClass();
      Object src = TermCompList.this;
      getSelectionManager().selectTerm(src,oboClass,getUseTermListener());
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
  

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
  
}
//   private OBOClass getCompListOboClass(int index) {
//     Object obj = defaultComboBoxModel.getElementAt(index);
//     return oboClassDowncast(obj);
//   }
