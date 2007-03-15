package phenote.datamodel;

import org.geneontology.oboedit.datamodel.OBOClass;


/** At the moment char field values can be Strings (genotype) and OBOClasses, and possibly
    more coming (Genotype?). This class attempts to hide the details of the actual
    data structure of the value for the field - could be String or OBOClass but can
    still deal with both the same - at least thats the idea... 
    post composition brings up some interesting issues...??? 
    should this be merged with CharField? im forgetting the rationale for having it be
    separate??? oh wait CharField is the generic field, CharFieldValue is an actual
    instance of data within the CharField - linked via CharFieldEnum & Character*/
public class CharFieldValue {

  private OBOClass oboClassValue=null;
  private String stringValue=null;
  private boolean isOboClass=true;
  private CharFieldEnum charFieldEnum;
  private CharField charField;
  // private CharField???
  private CharacterI character;
  //private boolean isDifferentia; // ??

  // phase out
  public CharFieldValue(String s,CharacterI c, CharFieldEnum e) {
    stringValue = s;
    isOboClass = false;
    character = c;
    charFieldEnum = e;
  }

  // for generic field - CharField or String???
  public CharFieldValue(String value,CharacterI c,CharField cf) {
    //System.out.println("CFV const "+value);
    this(c,cf);
    stringValue = value;
    isOboClass = false;
  }

  // phase out
  public CharFieldValue(OBOClass o,CharacterI c,CharFieldEnum e) {
    oboClassValue = o;
    isOboClass = true;
    character = c;
    charFieldEnum = e;
  }
  public CharFieldValue(OBOClass o,CharacterI c,CharField cf) {
    this(c,cf);
    oboClassValue = o;
    isOboClass = true;
  }

  private CharFieldValue(CharacterI c,CharField cf) {
    character = c;
    charField = cf;
  }

  // hmmmmm.... needed if post comp done inframe
//   public CharFieldValue(OBOClass o,CharacterI c,CharFieldEnum e, boolean isDifferentia) {
//     this(o,c,e);
//     this.isDifferentia = isDifferentia;
//   }

  static CharFieldValue emptyValue(CharacterI c,CharField cf) {
    if (cf.hasOntologies())
      return new CharFieldValue((OBOClass)null,c,cf);
    else
      return new CharFieldValue((String)null,c,cf); // ""?
  }

  boolean isEmpty() {
    if (isOboClass)
      return oboClassValue == null;
    else 
      return stringValue == null;
  }

  // maybe this should be called getString??? why getName???
  public String getName() { 
    if (!isOboClass)
      return stringValue;
    if (oboClassValue != null) // obo class may not be set yet
      return oboClassValue.getName();
    return ""; // null?
  }

  public boolean isTerm() { return isOboClass; }

  public OBOClass getOboClass() { return getTerm(); } // --> getTerm more general
  public OBOClass getTerm() { return oboClassValue; }

  public void editModel() {
//     if (charFieldEnum == null)
//       System.out.println("ERROR no datamodel associated with configuration, cant set"+
//                          " value");
    //else
//     if (charFieldEnum != null) { // pase - take out
//       charFieldEnum.setValue(character,this);
//     }
//     else {
      //System.out.println("CFV editMod "+getName());
    character.setValue(charField,this);
//    }
  }

  public CharField getCharField() { return charField; }
  // phase out...
  //public CharFieldEnum getCharFieldEnum() { return charFieldEnum; }

  public String toString() { return getName(); }
}

