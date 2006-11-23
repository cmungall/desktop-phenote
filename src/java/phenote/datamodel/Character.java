package phenote.datamodel;

import java.util.HashMap;
import java.util.Map;

import org.geneontology.oboedit.datamodel.OBOClass;

/** Characters are the EAV building blocks of a Phenotype. Previously this
    was called a Phenotype which was a misnomer 
    Should the Character datamodel be a generic list of CharFieldValues?
    That can be free text or from ontologies? hmmmm..... im starting to think
    this datamodel is too hardwired - hmmmm....*/
public class Character implements CharacterI, Cloneable {

  // List<CharFieldValue> charFields??? or List<CharField>
//   private String entity="";
//   private String quality="";
//   private String geneticContext="";
  private String pub;
  private String genotype=""; // eventually Genotype class
  // OboClass? OntologyTerm?...
  private OBOClass entity; // CharFieldValue???
  private OBOClass quality;
  private OBOClass geneticContext;
  private HashMap<CharField,CharFieldValue> charFieldToValue =
    new HashMap<CharField,CharFieldValue>();

  /** for generic fields its just a map from char field to char field value */
  public void setValue(CharField cf, CharFieldValue cfv) {
    charFieldToValue.put(cf,cfv);
    //System.out.println("Char setVal "+cf+" val "+cfv);
  }
  /** generic getter */ 
  public CharFieldValue getValue(CharField cf) {
    CharFieldValue cfv = charFieldToValue.get(cf);
    // if cfv is null should we construct a CFV w null/"", should field be init to ""?
    // undo needs to be able to undo back to null/""/init somehow
    if (cfv == null) {
      cfv = CharFieldValue.emptyValue(this,cf); // set in hash?
    }
//     if (cfv == null && cf.hasCharFieldEnum()) // phase out - i think this is phased out
//       cfv = cf.getCharFieldEnum().getValue(this);
    return cfv;
  }

  public String getValueString(String field) throws Exception {
    CharField cf = getCharFieldForName(field); // throws ex
    if (!hasValue(cf)) return null; // ?? exception?
    return getValue(cf).getName();
  }

  public OBOClass getTerm(String field) throws Exception {
    CharField cf = getCharFieldForName(field); // throws ex
    if (!hasValue(cf)) return null; // ?? exception?
    return getValue(cf).getOboClass();
  }

  // should this be isNull?
  public boolean hasValue(CharField cf) {
    // empty string is a valid value for non-required field - or should there
    // be some sort of somthing to indicate "empty" value?
    return getValue(cf) != null; // && !getValue(cf).equals("");
  }
  public boolean hasValue(String fieldName) throws Exception {
    return hasValue(getCharFieldForName(fieldName));
  }

  private CharField getCharFieldForName(String fieldName) throws Exception {
    for (CharField cf : charFieldToValue.keySet()) {
      if (cf.getName().equalsIgnoreCase(fieldName))
        return cf;
    }
    throw new Exception("No field for "+fieldName);
  }

  public String getPub() { return pub; }
  public boolean hasPub() { return pub!=null && !pub.equals(""); }
  public String getGenotype() { return genotype; }
  public OBOClass getEntity() { return entity; }
  public OBOClass getQuality() { return quality; }
  public boolean hasGeneticContext() {
    return geneticContext!=null && !geneticContext.equals("");
  }
  public OBOClass getGeneticContext() { return geneticContext; }

  // convenience functions
  public String getEntityName() { return entity.getName(); }
  public String getQualityName() { return quality.getName(); }
  public String getGeneticContextName() { return geneticContext.getName(); }


  public void setPub(String p ) { pub = p; }
  public void setGenotype(String gt) { genotype = gt; }
  public void setEntity(OBOClass e) { entity = e; }
  public void setQuality(OBOClass p) { quality = p; }
  public void setGeneticContext(OBOClass gc) { geneticContext = gc; }
 
  public boolean equals(CharacterI ch) {
    return eq(genotype,ch.getGenotype()) && eq(entity,ch.getEntity())
      && eq(quality,ch.getQuality()) && eq(geneticContext,ch.getGeneticContext());
  }

  /** check if both are null in addition to .equals() */
  private boolean eq(Object o1, Object o2) {
    if (o1==null && o2==null) return true;
    if (o1 == null) return false;
    return o1.equals(o2);
  }

  public CharacterI cloneCharacter() {
    try {
      // do OBOClasses clone? do we need to clone them?
      Character clone = (Character)clone();
      clone.charFieldToValue = (HashMap<CharField,CharFieldValue>)charFieldToValue.clone();
      return clone;
    } catch (CloneNotSupportedException e) { return null; }
  }

  public boolean hasNoContent() {
    if (hasPub()) return false;
    if (genotype!=null && !genotype.equals("")) return false;
    if (entity != null) return false;
    if (quality != null) return false;
    return geneticContext == null;
  }
 
  public String toString() {
    return "Pub "+pub+" genotype "+genotype+" gen ctxt "+geneticContext+" entity "+
      entity+" qual "+quality;
  }
}
