package phenote.gui.field;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOProperty;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldValue;

/** with now common superclass OBOObject this should be merged with TermCompList 
    I think */

class RelationCompList extends AbstractAutoCompList {

  private OBOProperty currentRel=null;

  RelationCompList(CharField c) {
    super(c);
    //setLabel("Relationship"); //does it get this from char field?
    checkPostCompButton(false); // cant post comp rels can you?
    enableEditModel(false); // doesnt directly edit model i dont think ever?
  }


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
    setText(rel.getName());
  }
  /** Throws exception if there isnt a current relation - for relation lists
      (post comp), if the user
      has typed something that isnt yet a rel - hasnt selected a rel */
  public OBOProperty getCurrentRelation() throws CharFieldGuiEx {
    if (currentRel == null) throw new CharFieldGuiEx("relation is empty");
    if (!currentRel.getName().equals(getText()))
      throw new CharFieldGuiEx("(relation "+currentRel.getName()+" and input "+getText()+
                          " dont match)");
    return currentRel;
    
  }


  /** returns currently selected relation, for auto combos of relations,
      throws obo ex if there is no current relation - this must be OBOProperty
      as OBOObject not sufficient for OBORestrictions */
  private OBOProperty getSelectedRelation() throws OboException {
    Object obj = getSelectedObject(); // throws oboex if no combobox model
    //return oboPropertyDowncast(obj); // throws oboex
    //CompletionRelation r = compRelDowncast(obj);
    CompletionObject r = compObjDowncast(obj);
    if (!r.hasOboProperty())
      throw new OboException("Rel list item is not a relation "+r);
    return r.getOboProperty();
  }
//   private CompletionRelation compRelDowncast(Object obj)  throws OboException {
//     if (obj == null) throw new OboException();
//     if ( ! (obj instanceof CompletionRelation)) {
//       //log.info("Item in completion list not obo class "+obj.getClass());
//       throw new OboException("Item in relation list not CompRel "+obj.getClass());
//     }
//     return (CompletionRelation)obj;
//   }
  private CompletionObject compObjDowncast(Object obj)  throws OboException {
    if (obj == null) throw new OboException();
    if ( ! (obj instanceof CompletionObject)) {
      //log.info("Item in completion list not obo class "+obj.getClass());
      throw new OboException("Item in relation list not CompObj "+obj.getClass());
    }
    return (CompletionObject)obj;
  }
   
  protected void updateModel(boolean useTopHit) {
    // todo: implement useTopHit for "return" on partial input! list TermComp
    try {
      this.setCurrentValidItem();
    } catch (OboException e) {
      // edit model enabled if in main window (not in pc/comp popups)
      if (!this.editModelEnabled()) {
        return;
      }
      log().debug(e);
      // TODO Auto-generated catch block
    }
  }

  /** no-op override */
  protected void setModelToNull() {}

  /** char in table changed - adjust - not needed for rel(at least not yet)
      as post comp doesnt listen to table changes (does it? should it?), 
      just term */ 
  protected void setCharFieldValue(CharFieldValue value) {}

  /** for now not threading and just sending results to listener - 
      relations are short lists - dont need threaded optimization
      so boolean thread is ignored */
  protected void getSearchItems(String input, SearchListener l, boolean thread)  {
    l.newResults(getCompListSearcher().getStringMatchRelations(input));
  }


  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}

  /** Returns a vector of CompletionRelations for auto completion
      which contain input, using search params */
//   protected Vector getSearchItems(String input) {
//     return getCompListSearcher().getStringMatchRelations(input);
//   }
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
//   private OBOProperty oboPropertyDowncast(Object obj)  throws OboException {
//     if (obj == null) throw new OboException();
//     if ( ! (obj instanceof OBOProperty)) {
//       //log.info("Item in completion list not obo class "+obj.getClass());
//       throw new OboException("Item in completion list not obo prop "+obj.getClass());
//     }
//     return (OBOProperty)obj;
//   }
