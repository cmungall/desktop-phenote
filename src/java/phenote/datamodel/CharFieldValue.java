package phenote.datamodel;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.impl.DanglingClassImpl;
import org.obo.util.TermUtil;


/** At the moment char field values can be Strings, OBOClasses, and dates, and possibly
    more coming. This class attempts to hide the details of the actual
    data structure of the value for the field - could be String or OBOClass but can
    still deal with both the same - at least thats the idea... 
    post composition brings up some interesting issues...??? 
    should this be merged with CharField? im forgetting the rationale for having it be
    separate??? oh wait CharField is the generic field, CharFieldValue is an actual
    instance of data within the CharField - linked via CharFieldEnum & Character*/
public class CharFieldValue implements Cloneable {

  private OBOClass oboClassValue=null;
  private String stringValue=null;
  private Date dateValue=null;
  //private boolean isOboClass=true;
  private CharFieldEnum charFieldEnum;
  private CharField charField;
  // private CharField???
  private CharacterI character;
  //private boolean isDifferentia; // ??

  // phase out
  public CharFieldValue(String s,CharacterI c, CharFieldEnum e) {
    stringValue = s;
    character = c;
    charFieldEnum = e;
  }

  // for string field 
  public CharFieldValue(String value,CharacterI c,CharField cf) {
    this(c,cf);
    stringValue = value;
  }

  // phase out
  public CharFieldValue(OBOClass o,CharacterI c,CharFieldEnum e) {
    oboClassValue = o;
    character = c;
    charFieldEnum = e;
  }
  public CharFieldValue(OBOClass o,CharacterI c,CharField cf) {
    this(c,cf);
    oboClassValue = o;
  }

  public CharFieldValue(Date d, CharacterI c, CharField cf) {
    this(c,cf);
    dateValue = d;
  }

  private CharFieldValue(CharacterI c,CharField cf) {
    character = c;
    charField = cf;
  }

  /** dateString is a date, if not valid date throws ParseEx */
  static CharFieldValue makeDate(String dateString, CharacterI c, CharField cf) 
    throws ParseException {
    CharFieldValue cfv = new CharFieldValue(c,cf);
    cfv.dateValue = DateFormat.getDateInstance().parse(dateString);
    return cfv;
  }

  CharFieldValue cloneCharFieldValue() {
    if (!charField.getCopyEnabled()) {
      CharFieldValue nullCloneValue = new CharFieldValue(character, charField);
      return nullCloneValue;
    }
//      return null; // null?? new CharFieldValue(character
    try { return (CharFieldValue)clone(); }
    catch (CloneNotSupportedException x) { return null; }
  }

  // hmmmmm.... needed if post comp done inframe
//   public CharFieldValue(OBOClass o,CharacterI c,CharFieldEnum e, boolean isDifferentia) {
//     this(o,c,e);
//     this.isDifferentia = isDifferentia;
//   }

  public static CharFieldValue emptyValue(CharacterI c,CharField cf) {
    // everything is null, type from char field
    return new CharFieldValue(c,cf);
//     if (cf.isTerm())
//       return new CharFieldValue((OBOClass)null,c,cf);
//     else if (cf.isDate())
//     else
//       return new CharFieldValue((String)null,c,cf); // ""?
  }

  void setCharacter(CharacterI c) { character = c; }
  public CharacterI getCharacter() { return character; }

  boolean isEmpty() {
    if (isTerm())
      return oboClassValue == null;
    else if (isDate())
      return dateValue == null;
    else 
      return ((stringValue == null) || (stringValue.equals("")));
  }

  // maybe this should be called getString??? why getName???
  // --> getValueAsString - much better name - phase out
  public String getName() { 
    return getValueAsString();
  }

  public String getValueAsString() {
    if (isEmpty()) return ""; // null?
    if (isTerm())
      return oboClassValue.getName();
    if (isDate())
      return DateFormat.getDateInstance().format(dateValue);
    if (!isTerm())
      return stringValue;
    if (oboClassValue != null) // obo class may not be set yet
      return oboClassValue.getName();
    return ""; // null?
  }

  public void setName(String name) {
    if (isTerm())
      oboClassValue.setName(name);
    else // this method is really for terms, but might as well...
      stringValue = name;
  }

  public boolean isTerm() { return getCharField().isTerm(); }

  public boolean isDate() { return getCharField().isDate(); }
  public Date getDate() { return dateValue; }

  public OBOClass getOboClass() { return getTerm(); } // --> getTerm more general
  public OBOClass getTerm() { return oboClassValue; }

  public void editModel() {
    if (isEmpty() && charField.isList()) // delete from list - cheesy?
      character.deleteValue(getCharField(),this);
    else
      character.setValue(charField,this);
    // could also edit obo edit annot model at this point! ??
  }

  public CharField getCharField() { return charField; }

  public boolean isDangler() {
    if (!isTerm()) return false;
    if (isEmpty()) return false;
    return TermUtil.isDangling(getTerm());
  }

  /** if cfv is a term just returns it, otherwise makes a dangling term out of 
      its value, string or date or whatnot */
  public OBOClass toTerm() {
    if (isTerm()) return getTerm();
    else {
      OBOClass term =
        new DanglingClassImpl(getCharField().getTag()+":"+getValueAsString());
      term.setName(getName());
      return term;
    }
  }

  public String toString() { return getValueAsString(); }


//     if (charFieldEnum == null)
//       System.out.println("ERROR no datamodel associated with configuration, cant set"+
//                          " value");
    //else
//     if (charFieldEnum != null) { // pase - take out
//       charFieldEnum.setValue(character,this);
//     }
//     else {
      //System.out.println("CFV editMod "+getName());
  // phase out...
  //public CharFieldEnum getCharFieldEnum() { return charFieldEnum; }
  
  public boolean equals(Object o) {
    final CharFieldValue otherValue = (CharFieldValue)o;
    if (this == otherValue) return true;
    if (this.isEmpty()) {
      return otherValue.isEmpty();
    }
    if (this.isTerm()) {
      return ((otherValue.isTerm()) && (this.getTerm().equals(otherValue.getTerm())));
    }
    return this.getName().equals(otherValue.getName());
  }
  
  public int hashCode() {
    if (this.oboClassValue != null) return this.oboClassValue.hashCode();
    if ((this.stringValue != null) || (!this.stringValue.equals(""))) return this.stringValue.hashCode();
    return 0;
  }
}

