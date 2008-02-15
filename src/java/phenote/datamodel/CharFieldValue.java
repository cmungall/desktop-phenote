package phenote.datamodel;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.obo.datamodel.OBOClass;
import org.obo.datamodel.impl.DanglingClassImpl;
import org.obo.util.TermUtil;


/** At the moment char field values can be Strings, OBOClasses, and dates, and possibly
    more coming. This class attempts to hide the details of the actual
    data structure of the value for the field - could be String or OBOClass but can
    still deal with both the same - at least thats the idea... 
    post composition brings up some interesting issues...??? 
    CharField is the generic field, CharFieldValue is an actual
    instance of data within the CharField - in a Character*/
public class CharFieldValue implements Cloneable {

  private static final Logger LOG = Logger.getLogger(CharFieldValue.class);

  /** trying this out, need lists, one way is recursion - CFV can be a list of CFVs */
  private List<CharFieldValue> charFieldValueList=null;
  private OBOClass oboClassValue=null;
  private String stringValue=null;
  private Date dateValue=null;
  //private boolean isOboClass=true;
  private CharFieldEnum charFieldEnum;
  private CharField charField;
  // private CharField???
  private CharacterI character;
  private boolean isList = false;
  private boolean overridePickList = false;
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

  /** SINGLE VALUE term/class - not list */
  public CharFieldValue(OBOClass o,CharacterI c,CharField cf) {
    this(c,cf);
    oboClassValue = o;
  }

  public static CharFieldValue makeListParentValue(CharacterI c, CharField cf) {
    CharFieldValue v = new CharFieldValue(c,cf);
    v.setIsList(true);
    return v;
  }

  /** If list adds to value, if not list just sets it, makes new CFV */
  public static CharFieldValue makeNewValue(OBOClass o,CharFieldValue oldVal) {
    CharFieldValue newVal = oldVal.cloneCharFieldValue(); // also clones list
    if (!newVal.isList()) { // SINGLE TERM
      newVal.oboClassValue = o;
    }
    else { // LIST/MULTI VALUE
      newVal.isList = true; // only parent
      CharFieldValue newKid = new CharFieldValue(o,oldVal.character,oldVal.charField);
      newVal.getCharFieldValueList().add(newKid);
    }
    return newVal;
  }
  /** If list adds to value, if not list just sets it, makes new CFV */
  public static CharFieldValue makeNewValue(String newString,CharFieldValue oldVal) {
    CharFieldValue newVal = oldVal.cloneCharFieldValue(); // also clones list
    if (!newVal.isList()) { // SINGLE TERM
      newVal.stringValue = newString;
    }
    else { // LIST/MULTI VALUE
      newVal.isList = true; // only parent
      CharFieldValue newKid =
        new CharFieldValue(newString,oldVal.character,oldVal.charField);
      newVal.getCharFieldValueList().add(newKid);
    }
    return newVal;
  }

  public CharFieldValue(Date d, CharacterI c, CharField cf) {
    this(c,cf);
    dateValue = d;
  }

  private CharFieldValue(CharacterI c,CharField cf) {
    character = c;
    charField = cf;
  }

  /** Parent list returns true, kid leaves false, single term returns false */
  private boolean isList() { return isList; }
  public void setIsList(boolean isList) { this.isList = isList; }
  public void setOverridePickList(boolean flag) { this.overridePickList = flag; }
  
  // initialize if null?
  private List<CharFieldValue> getList() { return charFieldValueList; }

  public List<CharFieldValue> getCharFieldValueList() {
    if (charFieldValueList == null) // ???
      charFieldValueList = new ArrayList<CharFieldValue>();
    return charFieldValueList;
  }

  /** dateString is a date, if not valid date throws ParseEx */
  static CharFieldValue makeDate(String dateString, CharacterI c, CharField cf) 
    throws ParseException {
    CharFieldValue cfv = new CharFieldValue(c,cf);
    cfv.dateValue = DateFormat.getDateInstance().parse(dateString);
    return cfv;
  }

  public CharFieldValue cloneCharFieldValue() {
    // some fields may disable copying, eg db id fields
    if (!charField.getCopyEnabled()) {
      CharFieldValue nullCloneValue = new CharFieldValue(character, charField);
      return nullCloneValue;
    }
//      return null; // null?? new CharFieldValue(character
    try {
      CharFieldValue clone =  (CharFieldValue)clone();
      if (charFieldValueList!=null) {
        clone.charFieldValueList = new ArrayList<CharFieldValue>();
        for (CharFieldValue v : charFieldValueList) {
          clone.charFieldValueList.add(v); // should v itself be cloned???
        }
      }
      return clone;
    }
    catch (CloneNotSupportedException x) { return null; }
  }
  
  public CharFieldValue cloneValueForChar(CharacterI newChar) {
    LOG.debug("Cloning charfield value: " + this.toString());
    final CharFieldValue newValue = this.cloneCharFieldValue();
    if (newValue == null) return null;
    if (newChar != null) newValue.character = newChar;
    return newValue;    
  }

  public CharFieldValue cloneCharFieldValue(CharacterI newCharacter, CharField newField) {
    CharFieldValue newValue = cloneValueForChar(newCharacter);
    if (newField != null) newValue.charField = newField;
    return newValue;
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

  public boolean isEmpty() {
    if (isList || isPickList()) {
      if (charFieldValueList == null || charFieldValueList.isEmpty())
        return true;
      return charFieldValueList.get(0).isEmpty();
    }
    else if (isTerm())
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
    if (isList() || isPickList()) { // parent true, kids false - stops recursion
      String s="";
      for (CharFieldValue kid : getCharFieldValueList())
        s += '"'+kid.getValueAsString()+'"' + CharField.LIST_DELIM;
      if (s.length() > 0) s= s.substring(0,s.length()-1); // lopp off last comma
      return s;
    }
      
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

  /** this is used for danglers on loading data */	
  public void setName(String name) {
    if (isTerm())
      oboClassValue.setName(name);
    else // this method is really for terms, but might as well...
      stringValue = name;
  }

  public boolean isTerm() { return getCharField().isTerm(); }
  /** convenience fn */
  public String getID() {
    if (isEmpty()) return ""; // ??
    if (!isTerm()) return ""; // null? ex?
    if (isList()) { // should IDs get quoted? i guess to be consistent seems silly
      StringBuilder sb = new StringBuilder();
      for (int i=0; i < getList().size(); i++) {
        sb.append('"').append(getList().get(i).getID()).append('"');
        if (i != getList().size() - 1)
          sb.append(CharField.LIST_DELIM);
      }
      return sb.toString();
    }
    return getTerm().getID();
  }

  public boolean isDate() { return getCharField().isDate(); }
  public Date getDate() { return dateValue; }
  
  public boolean isPickList() {
    if (this.overridePickList) {
      return false;
    } else {
      return this.getCharField().isPickList(); 
    }
  }
  
  public List<CharFieldValue> getValuePickList() {
    final CharField sourceField = this.getCharField().getPickListSourceField();
    return this.getCharacter().getValueList(sourceField);
  }

  public OBOClass getOboClass() { return getTerm(); } // --> getTerm more general
  public OBOClass getTerm() { return oboClassValue; }

  public void editModel() {
    // doesnt work - this is removing an alreadyempty val from list silly
//     if (isEmpty() && charField.isList()) // delete from list - cheesy?
//       character.deleteValue(getCharField(),this);
//     else
    character.setValue(charField,this);
    // could also edit obo edit annot model at this point! ??
  }

//   /** remove cfv from list of cfvs in char */
//   public void editModelDelete() {
//     //....//character.deleteValue(getCharField(),this);
//     // remove self from parent list... back pointer to parent?
//     character.getValue(charField).removeKid(this); // ??
//   }

  public void addKid(CharFieldValue kid) {
    // creates list if null
    getCharFieldValueList().add(kid);
  }

  /** Remove "kid" from kid list */
  public void removeKid(CharFieldValue kid) {
    if (charFieldValueList == null) {
      LOG.error("cant remove kid - list is null"); // ??
      return;
    }
    charFieldValueList.remove(kid);
  }
  
  public void removeAllKids() {
    if (charFieldValueList == null) {
      LOG.error("cant remove kids - list is null"); // ??
      return;
    }
    charFieldValueList.clear();
  }

  public CharField getCharField() { return charField; }

  public boolean isDangler() {
    if (!isTerm()) return false;
    if (isEmpty()) return false;
    return TermUtil.isDangling(getTerm());
  }

  /** if cfv is a term just returns it, returns null if doesnt have term,
   *  otherwise makes a dangling term out of 
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
    if (!(o instanceof CharFieldValue)) return false; // just in case
    final CharFieldValue otherValue = (CharFieldValue)o;
    if (this == otherValue) return true;
    if (this.isEmpty()) {
      return otherValue.isEmpty();
    }
    if (isList) {
      if (!otherValue.isList) return false;
      List<CharFieldValue> list2 = otherValue.charFieldValueList;
      if (charFieldValueList==null && list2==null) return true;
      if (charFieldValueList==null || list2==null) return false;
      if (charFieldValueList.size() != list2.size()) return false;
      for (CharFieldValue v1 : charFieldValueList) {
        if (!list2.contains(v1)) return false; // does equals on kids
      }
      return true; // lists are same
    }
    if (this.isTerm()) {
      return ((otherValue.isTerm()) 
              && (this.getTerm().equals(otherValue.getTerm())));
    }
    return this.getName().equals(otherValue.getName());
  }
  
  public int hashCode() {
    if (this.oboClassValue != null) return this.oboClassValue.hashCode();
    if ((this.stringValue != null) || (!this.stringValue.equals(""))) return this.stringValue.hashCode();
    return 0;
  }
}

