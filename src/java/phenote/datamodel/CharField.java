package phenote.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOSession;

// or just Field? or CharField?
// CharField doesnt handle instance data, just specifies what ontologies are 
// associated with what parts of the generic character
// CharFieldValue handles instance data
// CharField gets specified in the configuration - but oddly enough it better
// no contradit CharacterI - as in OBOClasses better have ontologies - this is
// funny i think
public class CharField {

  private List<Ontology> ontologyList = new ArrayList<Ontology>(3);
  private CharFieldEnum charFieldEnum; // or subclass
  private String name; // display name
  private String tag; // non display tag to refer to field (no spaces...)
  private boolean postCompAllowed=false;
  private Ontology postCompRelOntol;
  // RELATIONSHIP? DANGLER? INSTANCE? use CharFieldEnum?
  public enum Type { TERM, FREE_TEXT, INT }; 
  private Type type = Type.FREE_TEXT; // free text default, bkwrd compat
  // index? orderNumber? for order in gui/datamodel?

  /** used for relationship */
  public CharField(CharFieldEnum c) {
    charFieldEnum = c;
  }

  /** a generic field with no char field enum - get hip */
  public CharField(String name,String tag) {
    this.name = name;
    this.tag = tag;
  }

  /** part of constructor? probably */
  public void setType(Type t) {
    this.type = t;
  }

  public void addOntology(Ontology o) {
    ontologyList.add(o);
    // ??? if you have ontologies you better be a Term
    type = Type.TERM;
  }

  public void setName(String n) {
    name = n;
  }

  public String getName() {
    if (name == null) { // not explicitly set
      if (hasOneOntology()) {
        name =  getOntology().getName();
      }
      else
        name = charFieldEnum.toString();
    }
    return name;
  }

  public boolean isField(String tag) {
    if (tag.equalsIgnoreCase(this.tag)) return true;
    if (tag.equalsIgnoreCase(name)) return true;
    // check with _ substition for spaces?? shouldnt have to
    return false;
  }

  public CharFieldEnum getCharFieldEnum() { return charFieldEnum; }

  /** generic fields dont have enums */
  boolean hasCharFieldEnum() { return charFieldEnum != null; }

  public boolean isRelationship() {
    return charFieldEnum == CharFieldEnum.RELATIONSHIP;
  }

  //boolean isGeneticContext() { return charFieldEnum == CharFieldEnum.GENETIC_CONTEXT; }

  public List<Ontology> getOntologyList() { return ontologyList; }

  //booelan isFreeText() return !hasOntologies() ??
  public boolean isFreeText() {		//  now have third type isInt, so can't just negate 2007 07 09
    //if ( hasOntologies() || isInt() )  return false; 
    //return true; 
    return type == Type.FREE_TEXT;
  }

  public boolean isInt() {		//  now have third type isInt, so can't just negate 2007 07 09
    //if ( hasOntologies() || isFreeText() )  return false; 
    //return true; 
    return type == Type.INT;
  }

  /** This should be redundant with hasOntologies (though with instances this
      may change?) */
  public boolean isTerm() {
    return type == Type.TERM;
  }

  public boolean hasOntologies() {
    return ontologyList != null && !ontologyList.isEmpty();
  }
  public boolean hasOneOntology() {
    return hasOntologies() && getOntologySize() == 1;
  }
  public boolean hasMoreThanOneOntology() {
    return hasOntologies() && !hasOneOntology(); 
  }
  
  public Ontology getOntology() {
    if (!hasOntologies()) return null;
    return getFirstOntology();
  }
  public Ontology getFirstOntology() {
    if (ontologyList.isEmpty()) return null; // ex?
    return ontologyList.get(0);
  }

  private int getOntologySize() {
    if (!hasOntologies()) return 0;
    return ontologyList.size();
  }

  public boolean hasOntology(String ontologyName) {
    return getOntologyForName(ontologyName) != null; 
  }

  /** Returns Ontology with name ontologyName (ignores case), ont ex if dont have it */
  public Ontology getOntologyForName(String ontologyName){
    for (Ontology o : getOntologyList()) {
      if (o.getName().equalsIgnoreCase(ontologyName))
        return o;
    }
    return null;
  }

  // set whether post composition allowed (from config) */
  public void setPostCompAllowed(boolean pca) {
    postCompAllowed = pca;
  }

  /** whether this field allows for post composition - from config - allowed? how bout has? */
  public boolean postCompAllowed() {
    return postCompAllowed;
  }

  public void setPostCompRelOntol(Ontology o) {
    postCompRelOntol = o;
  }

  public Ontology getPostCompRelOntol() { return postCompRelOntol; }

  /** if free text returns string charfieldValue, for ontology field valueString is
      id and searches ontologies for id, throws ontologyException if not found 
      this needs to deal with post comp!!!! */
  public CharFieldValue makeValue(Character c, String valueString)
    throws TermNotFoundException {

    // FREE TEXT FIELD
    if (!hasOntologies())
      return new CharFieldValue(valueString,c,this);

    // ONTOLOGY
    else {

      OBOClass oboClass=null;

      // CHECK FOR POST COMP - could probably move this to char field but right now just
      // trying to get this working before i split town...
      if (OntologyManager.inst().isPostComp(valueString)) {
        try {
          // just gets first ontology for now - fix this!
          OBOSession os = getOntology().getOboSession();
          oboClass = OntologyManager.inst().getPostComp(os,valueString);
        }
        catch (TermNotFoundException e) {} // move on to next ontology
        
      }
      
      // TERM not post comp
      else {
        for (Ontology ont : ontologyList) {
          try { oboClass = ont.getTerm(valueString); }
          catch (TermNotFoundException e) {} // move on to next ontology
        }
      }
      if (oboClass != null)
        return new CharFieldValue(oboClass,c,this);
      else
        throw new TermNotFoundException(valueString+" not found in ontologies for "+this);
    }
  }

  public String toString() { return "CharField: "+getName(); }
}

