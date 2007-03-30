package phenote.gui.field;

import java.util.List;
//import java.util.Vector;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOProperty;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;


class RelationCompList extends AbstractAutoCompList {

  private OBOProperty currentRel=null;

  RelationCompList(CharField c) {
    super(c);
    //setLabel("Relationship"); //does it get this from char field?
    allowPostCompButton(false); // cant post comp rels can you?
    enableEditModel(false); // doesnt directly edit model i dont think ever?
    enableListeners(false); // at least while in comp window
  }

//   RelationCompList(CompListSearcher searcher,boolean editModel,CharField c) {
//     super(searcher,editModel,c);
    // this inner class enables retrieving of JList for mouse over
    // this will probably throw errors if non metal look & feel is used
//    setUI(new MetalListComboUI());
    //setFont(new Font("Courier",Font.PLAIN,12));

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
    //}

  /** The user has selected a rel from the list, validate and set current rel
      if doesnt validate throw ex */
  protected void setCurrentValidItem() throws OboException {
    setRel(getSelectedRelation());
  }

  protected String getCurrentTermRelName() {
    if (currentRel!= null)
      return currentRel.getName();
    else
      return "";
  }

  /** for relationships (post comp rel) */
  void setRel(OBOProperty rel) {
    if (rel == null) {
      log().error("Attempt to set term to null");
      return; // debug stack trace?
    }
    currentRel = rel;
    setText(rel.getName(),false); // eventually .getName()
  }
  /** Throws exception if there isnt a current relation - for relation lists
      (post comp), if the user
      has typed something that isnt yet a rel - hasnt selected a rel */
  OBOProperty getCurrentRelation() throws CharFieldGuiEx {
    if (currentRel == null) throw new CharFieldGuiEx("relation is null");
    if (!currentRel.getName().equals(getText()))
      throw new CharFieldGuiEx("(relation "+currentRel.getName()+" and input "+getText()+
                          " dont match)");
    return currentRel;
    
  }


  private CompletionRelation compRelDowncast(Object obj)  throws OboException {
    if (obj == null) throw new OboException();
    if ( ! (obj instanceof CompletionRelation)) {
      //log.info("Item in completion list not obo class "+obj.getClass());
      throw new OboException("Item in relation list not CompRel "+obj.getClass());
    }
    return (CompletionRelation)obj;
  }

  /** returns currently selected relation, for auto combos of relations,
      throws obo ex if there is no current relation */
  private OBOProperty getSelectedRelation() throws OboException {
    Object obj = getSelectedObject(); // throws oboex
    //return oboPropertyDowncast(obj); // throws oboex
    CompletionRelation r = compRelDowncast(obj);
    return r.getOboProperty();
  }
   
  /** no-op - relation list doesnt edit model (at least presently) */
  protected void editModel() {}

  /** char in table changed - adjust - not needed for rel(at least not yet)
      as post comp doesnt listen to table changes (does it? should it?), 
      just term */
  protected void setValueFromChar(CharacterI chr) {}

  /** Returns a vector of CompletionRelations for auto completion
      which contain input, using search params */
//   protected Vector getSearchItems(String input) {
//     return getCompListSearcher().getStringMatchRelations(input);
//   }
  protected List getSearchItems(String input)  {
    return getCompListSearcher().getStringMatchRelations(input);
  }


  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}

//   private OBOProperty oboPropertyDowncast(Object obj)  throws OboException {
//     if (obj == null) throw new OboException();
//     if ( ! (obj instanceof OBOProperty)) {
//       //log.info("Item in completion list not obo class "+obj.getClass());
//       throw new OboException("Item in completion list not obo prop "+obj.getClass());
//     }
//     return (OBOProperty)obj;
//   }
