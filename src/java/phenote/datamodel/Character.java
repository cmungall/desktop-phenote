package phenote.datamodel;

import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOClass;

/** Characters are the EAV building blocks of a Phenotype. Previously this
    was called a Phenotype which was a misnomer 
    Should the Character datamodel be a generic list of CharFieldValues?
    That can be free text or from ontologies? hmmmm..... im starting to think
    this datamodel is too hardwired - hmmmm....*/
public class Character implements CharacterI, Cloneable {

  // List<CharFieldValue> charFields??? or List<CharField>
  // phase these out...
  //private String pub;
  //private String genotype=""; // eventually Genotype class
  // OboClass? OntologyTerm?...
  //private OBOClass entity; // CharFieldValue???
  //private OBOClass quality;
  //private OBOClass geneticContext;
  // new generic data structure    try { setValue(new CharFieldValue(e,this,getEntityField())); }

  private HashMap<CharField,CharFieldValue> charFieldToValue =
    new HashMap<CharField,CharFieldValue>();

  // obo edit annotation that gets modified in setValues
  // private OBOEditAnntotation - get & set methods

  /** for generic fields its just a map from char field to char field value */
  public void setValue(CharField cf, CharFieldValue cfv) {
    charFieldToValue.put(cf,cfv);
    //System.out.println("Char setVal "+cf+" val "+cfv);
    // setOboEditModel(oboEditAnnotation,cf,cfv);
  }
  private void setValue(CharFieldValue cfv) {
    setValue(cfv.getCharField(),cfv);
  }

  public void setValue(CharField cf, String s) throws TermNotFoundException {
    CharFieldValue cfv = cf.makeValue(this,s);
    setValue(cf,cfv);
  }
  private void setValue(CharField cf, OBOClass term) {
    setValue(new CharFieldValue(term,this,cf));
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

  // return "" if null value?? throw ex? used for sorting CharList
  public String getValueString(CharField cf) {
    CharFieldValue cfv = getValue(cf);
    if (cfv.getName() == null) return ""; // ?? ex?
    return cfv.getName();
  }

  public String getValueString(String field) throws CharFieldException {
    CharField cf = getCharFieldForName(field); // throws ex
    if (!hasValue(cf)) return null; // ?? exception? ""?
    return getValue(cf).getName();
  }

  public OBOClass getTerm(String field) throws CharFieldException {
    CharField cf = getCharFieldForName(field); // throws ex
    return getTerm(cf);
  }

  private OBOClass getTerm(CharField cf) {
    if (!hasValue(cf)) return null; // ?? exception?
    return getValue(cf).getOboClass();
  }

  // should this be isNull?
  public boolean hasValue(CharField cf) {
    // empty string is a valid value for non-required field - or should there
    // be some sort of somthing to indicate "empty" value?
    //return getValue(cf) != null; // && !getValue(cf).equals("");
    if (getValue(cf) == null) return false;
    return !getValue(cf).isEmpty();
  }
  public boolean hasValue(String fieldName) {
    try {
      return hasValue(getCharFieldForName(fieldName));
    } 
    catch (CharFieldException e) { // throws exception if doesnt have
      return false;
    }
  }

  public CharField getCharFieldForName(String fieldName) throws CharFieldException {
    for (CharField cf : getAllCharFields()) {
      if (cf.getName().equalsIgnoreCase(fieldName))
        return cf;
    }
    throw new CharFieldException("No field for "+fieldName);
  }

  /** return all char fields configured, both currently used or null in char */
  private List<CharField> getAllCharFields() {
    return OntologyManager.inst().getCharFieldList();
  }

  // conveneince methods...
  public String getPub() { 
    try { return getValueString(getPubField()); }
    catch (CharFieldException e) { return null; } // ?? ""?
  }
  public boolean hasPub() { return getPub()!=null && !getPub().equals(""); }
  private CharField getPubField() throws CharFieldException {
    return getCharFieldForName(CharFieldEnum.PUB.getName());
  }
  public String getGenotype() {
    try { return getValueString(getGenotypeField()); }
    catch (CharFieldException e) { return null; } // ??
  }
  private CharField getGenotypeField() throws CharFieldException {
    return getCharFieldForName(CharFieldEnum.GENOTYPE.getName());
  }
  /** cant decide to kill this or not - part of me thinks entity is fundamental to
      phenote so why not make it handy */
  public OBOClass getEntity() {
    try { return getValue(getEntityField()).getTerm(); }
    catch (CharFieldException e) { return null; } // ??
  }
  private CharField getEntityField() throws CharFieldException {
    return getCharFieldForName(CharFieldEnum.ENTITY.getName());
    //catch (CharFieldException e) { return null; } // ?? ex?
  }
  public OBOClass getQuality() {
    //return quality;
    try { return getValue(getQualField()).getTerm(); }
    catch (CharFieldException e) { return null; } // ??
  }
  private CharField getQualField() throws CharFieldException {
    return getCharFieldForName(CharFieldEnum.QUALITY.getName());
  }
  public boolean hasGeneticContext() {
    return getGeneticContext()!=null && !getGeneticContext().equals("");
  }
  public OBOClass getGeneticContext() {
    try { return getTerm(getGenConField()); }
    catch (CharFieldException e) { return null; } // ??
  }
  private CharField getGenConField() throws CharFieldException {
    return getCharFieldForName(CharFieldEnum.GENETIC_CONTEXT.getName());
  }


  public void setPub(String p ) {  
    try { setValue(new CharFieldValue(p,this,getPubField())); }
    catch (CharFieldException x) { log().error("no Pub field -> config!"); }
  }
  public void setGenotype(String gt) {
    try { setValue(getGenotypeField(),gt); }
    catch (CharFieldException x) { log().error(x); }
    catch (TermNotFoundException e) {} // doesnt happen for free text field
 }
  public void setEntity(OBOClass e) {
    try { setValue(new CharFieldValue(e,this,getEntityField())); }
    catch (CharFieldException x) { log().error("no Entity field -> config!"); }
  }
  public void setQuality(OBOClass q) {
    try {setValue(new CharFieldValue(q,this,getQualField())); }
    catch (CharFieldException e) { log().error("no Quality field -> config!"); }
  }
  public void setGeneticContext(OBOClass gc) {
    try { setValue(getGenConField(),gc); }
    catch (CharFieldException e) { log().error("no Genetic Context field -> config!"); }
  }
 
  // this is just used for testing at this point
  public boolean equals(CharacterI ch) {
    //return eq(getGenotype(),ch.getGenotype()) && eq(getEntity(),ch.getEntity())
    //  && eq(quality,ch.getQuality()) && eq(geneticContext,ch.getGeneticContext());
    for (CharField cf : getAllCharFields()) {
      if (!eq(getValue(cf),ch.getValue(cf)))
        return false;
    }
    return true;
  }

  /** check if both are null in addition to .equals() */
  private boolean eq(CharFieldValue c1,CharFieldValue c2) {
    if (c1==null && c2==null) return true;
    if (c2 == null) return false;
    return c1.equals(c2);
  }

  public CharacterI cloneCharacter() {
    try {
      // do OBOClasses clone? do we need to clone them?
      Character clone = (Character)clone();
      clone.charFieldToValue = (HashMap<CharField,CharFieldValue>)charFieldToValue.clone();
      return clone;
    } catch (CloneNotSupportedException e) { return null; }
  }

  // used by character table panel
  public boolean hasNoContent() {
//     if (hasPub()) return false;
//     if (genotype!=null && !genotype.equals("")) return false;
//     if (getEntity() != null) return false;
//     if (getQuality() != null) return false;
//     return geneticContext == null;
    for (CharField cf : getAllCharFields()) {
      if (hasValue(cf)) return false;
    }
    return true;
  }
 
  public String toString() {
    //return "Pub "+pub+" genotype "+genotype+" gen ctxt "+geneticContext+" entity "+
    //  entity+" qual "+quality;
    StringBuffer sb = new StringBuffer();
    for (CharField cf : getAllCharFields()) {
      sb.append(cf.getName()).append(" ").append(charFieldToValue.get(cf)).append(" ");
    }
    return sb.toString();
  }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
