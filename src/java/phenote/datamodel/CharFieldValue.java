package phenote.datamodel;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOObject;
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
  private CharField charField;
  // private CharField???
  private CharacterI character;
  private boolean isList = false;
  private boolean overridePickList = false;
  //private boolean isComparison = false;
  private Comparison comparison; // inner class
  //private boolean isDifferentia; // ??

  // for string field 
  public CharFieldValue(String value,CharacterI c,CharField cf) {
    this(c,cf);
    stringValue = value;
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
  
  /** If list adds to value, if not list just sets it, makes new CFV 
   This is used by CharacterListFieldGui (for template) but i dont understand how it
  works for lists - newList and newValue never seem to connect - jim???*/
  public static CharFieldValue makeNewValue(CharFieldValue newValue,
                                            CharFieldValue oldValue) {
    final CharFieldValue updatedValue =
      newValue.cloneCharFieldValue(oldValue.getCharacter(), oldValue.getCharField());
    if (oldValue.isList()) { // LIST/MULTI VALUE
      CharFieldValue newList = oldValue.cloneCharFieldValue();
      newList.isList = true; // only parent
      newValue.getCharFieldValueList().add(updatedValue);
      return newList;
    } else {
      return updatedValue;
    }
  }

  public CharFieldValue(Comparison comp) {
    charField = CharFieldManager.inst().getComparisonField();
    comparison = comp;
    character = comp.getSubject();
  }

//   /** explicit comparison constructor - subclasses i suppose would be good */
//   public static CharFieldValue makeComparison(CharField cf,CharacterI subject,
//                                               OBOProperty rel,CharacterI object) {
//     return new CharFieldValue(cf,subject,rel,object);
//   }
//   /** comparison constructor */
//   private CharFieldValue(CharField cf, CharacterI s,OBOProperty r,CharacterI o) {
//     //isComparison = true;
//     charField = cf; // check if comparison?
//     comparison = new Comparison(s,r,o);
//   }

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
      // basic stuff
      CharFieldValue clone =  (CharFieldValue)clone();
      // comparison
      if (comparison!=null)
        clone.comparison = comparison.cloneComparison();
      // list
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
      return charFieldValueList == null;
    } else if (isTerm())
      return oboClassValue == null;
    else if (isDate())
      return dateValue == null;
    else if (isComparison())
      return comparison == null;
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
    if (isComparison())
      return comparison.toString();
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
  
  public boolean isComparison() { return getCharField().isComparison(); }
  
  /** From cfv comparison parent */
  public List<Comparison> getComparisonList() {
    if (!isList()) { // shouldnt happen
      LOG.error("Calling getCompList on non-list");
      return null; // ex? empty list?
    }
    List<Comparison> l = new ArrayList<Comparison>();
    for (CharFieldValue kid : getCharFieldValueList())
      l.add(kid.getComparison());
    return l;
  }
  /** from comparison kid */
  private Comparison getComparison() { return comparison; }

  public boolean isCompound() {
    return this.getCharField().isCompound();
  }
  
  public List<CharFieldValue> getValuePickList() {
    final CharField sourceField = this.getCharField().getPickListSourceField();
    return this.getCharacter().getValueList(sourceField);
  }

  public OBOClass getOboClass() { return getTerm(); } // --> getTerm more general
  public boolean isTerm() { return getCharField().isTerm(); }
  public OBOClass getTerm() { return oboClassValue; }

  /** an OBOObject can be an OBOClass or an OBOProperty and other obo thingies 
      return true if is term and obj is oboClassValue
  using oboObject instead of OBOClass so can use same method for other obo objects */
  public boolean hasObject(OBOObject obj) {
    if (isTerm())
      return getTerm().equals(obj);
    // if isRelation...
    return false;
  }

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
  
  public void insertKid(int index, CharFieldValue kid) {
    getCharFieldValueList().add(index, kid);
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

  public boolean hasKid(CharFieldValue kid) {
    return getCharFieldValueList().contains(kid);
  }

  /** return kid that has same value/class/string as val. if none return null */
  public CharFieldValue getKidWithSameValue(CharFieldValue val) {
    if (getList() == null) return null; // shouldnt happen
    for (CharFieldValue kid : getList()) {
      if (kid.equals(val)) // equals tests values
        return kid;
    }
    return null;
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
        new DanglingClassImpl(getValueAsString()); // TODO - cjm - use normal OBO class?
      //term.setName(getName());
      return term;
    }
  }

  public String toString() { return getValueAsString(); }

//   private class Comparison {
//     private CharacterI subject;
//     private OBOProperty rel;
//     private CharacterI object;
//     private Comparison(CharacterI s, OBOProperty r,CharacterI o) {
//       subject = s; rel = r; object = o;
//     }
//     public String toString() {
//       String objString = object.toString(); // yucky
//       if (object.hasAnnotId()) objString = object.getAnnotId(); // better
//       return rel.getName()+"^("+objString+")";
//     }
//   }

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
  
  /** returns true if the actual values are equal. 1st of all o must be a 
      CharFieldValue. then tests for equality of of value, that is term,string,
      list items, .... So CharFieldValues from 2 different characters with same
      obo class are equal */
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
        // works because unique and same size list
        if (!list2.contains(v1)) return false; // does equals on kids,
      }
      return true; // lists are same
    }
    if (this.isTerm()) {
      return ((otherValue.isTerm()) 
              && (this.getTerm().equals(otherValue.getTerm())));
    }
    // Comparison??
    return this.getName().equals(otherValue.getName());
  }
  
  public int hashCode() {
    if (this.oboClassValue != null) return this.oboClassValue.hashCode();
    if ((this.stringValue != null) && (!this.stringValue.equals(""))) return this.stringValue.hashCode();
    return 0;
  }
}

