package phenote.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;

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
  private String name;
  private boolean postCompAllowed=false;
  private Ontology postCompRelOntol;

  public CharField(CharFieldEnum c) {
    charFieldEnum = c;
  }

  /** a generic field with no char field enum - get hip */
  public CharField(String name) {
    this.name = name;
  }

  public void addOntology(Ontology o) {
    ontologyList.add(o);
  }

  public void setName(String n) {
    name = n;
  }

  public String getName() {
    if (name == null) { // not explicitly set
      if (hasOneOntology())
        name =  getOntology().getName();
      else
        name = charFieldEnum.toString();
    }
    return name;
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
  public Ontology getFirstOntology() { return ontologyList.get(0); }

  private int getOntologySize() {
    if (!hasOntologies()) return 0;
    return ontologyList.size();
  }

  public boolean hasOntology(String ontologyName) {
    return getOntologyForName(ontologyName) != null;
  }

  /** Returns Ontology with name ontologyName (ignores case), null if dont have it */
  public Ontology getOntologyForName(String ontologyName) {
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

  /** whether this field allows for post composition - from config */
  public boolean postCompAllowed() {
    return postCompAllowed;
  }

  public void setPostCompRelOntol(Ontology o) {
    postCompRelOntol = o;
  }

  public Ontology getPostCompRelOntol() { return postCompRelOntol; }

  /** if free text returns string charfieldValue, for ontology field valueString is
      id and searches ontologies for id, throws ontologyException if not found */
  public CharFieldValue makeValue(Character c, String valueString)
    throws TermNotFoundException {
    if (!hasOntologies())
      return new CharFieldValue(valueString,c,this);
    else {
      OBOClass oboClass=null;
      for (Ontology ont : ontologyList) {
        try { oboClass = ont.getOboClass(valueString); }
        catch (TermNotFoundException e) {} // move on to next ontology
      }
      if (oboClass != null)
        return new CharFieldValue(oboClass,c,this);
      else
        throw new TermNotFoundException(valueString+" not found in ontologies for "+this);
    }
  }

  public String toString() { return "CharField: "+getName(); }
}

    // is this getting silly? abstract? --> char field value i think
    //public void setOboClass(CharacterI c, OBOClass o) {}
    //public OBOClass getOBOClass(CharacterI c) { return null; }
//   public CharField(CharFieldEnum c, Ontology o) {
//     charFieldEnum = c;
//     ontologyList.add(o);
//   }

//   public CharField(CharFieldEnum c, String n) {
//     charFieldEnum = c;
//     name = n;
//   }

// hmmmmmm.... wrap String & OBOClass in one class
// public class CharFieldValue {
// OBOClass oboClassValue
// String stringValue
// CharFieldValue(String)
// CharFieldValue(OBOClass)
// getName() { if isObo return obo.getName; else return string
// getid, ....
// }

// above would be setValue(CharFieldValue)
// getValue(CharFieldValue)
  // separate class? labels? methods? subclasses?
  // is this taking enums too far? or a good use of them?
  // would it be nice to have a class that wrapped String and OBOClass?
  // and all possible field values? or would that be annoying?
//   public enum CharFieldEnum {

//     PUB("Pub") {
//       public void setValue(CharacterI c, CharFieldValue v) {
//         c.setPub(v.getName());
//       }
//       public CharFieldValue getValue(CharacterI c) {
//         return new CharFieldValue(c.getPub(),c,this);
//       }
//     },
//     LUMP("Genotype") { // genotype? default?
//       public void setValue(CharacterI c, CharFieldValue v) {
//         c.setGenotype(v.getName());
//       }
//       public CharFieldValue getValue(CharacterI c) {
//         return new CharFieldValue(c.getGenotype(),c,this);
//       }
//     },
//     GENETIC_CONTEXT("Genetic Context") {
//       public void setValue(CharacterI c, CharFieldValue v) {
//         c.setGeneticContext(v.getOboClass());
//       }
//       public CharFieldValue getValue(CharacterI c) {
//         return new CharFieldValue(c.getGeneticContext(),c,this);
//       }
//     },
//     ENTITY("Entity") {
//       public void setValue(CharacterI c, CharFieldValue v) {
//         c.setEntity(v.getOboClass());
//       }
//       public CharFieldValue getValue(CharacterI c) {
//         return new CharFieldValue(c.getEntity(),c,this);
//       }
//     },
//     QUALITY("Quality") {
//       public void setValue(CharacterI c, CharFieldValue v) {
//         c.setQuality(v.getOboClass());
//       }
//       public CharFieldValue getValue(CharacterI c) {
//         return new CharFieldValue(c.getQuality(),c,this);
//       }
//     };

