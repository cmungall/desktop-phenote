package phenote.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/** The Character datamodel is a generic hash of CharField-CharFieldValues
    CharFieldValues are free text or from ontologies
    This is the straightontabular implementation of CharacterI
    In the table view a character represents a row */
public class Character extends AbstractCharacter implements CharacterI {

  // should there be a CharFieldValList explicit object?
//   private HashMap<CharField,List<CharFieldValue>> charFieldToValue =
//     new HashMap<CharField,List<CharFieldValue>>();
   private HashMap<CharField,CharFieldValue> charFieldToValue =
     new HashMap<CharField,CharFieldValue>();

  /** Comparison is an inner class */
  private List<Comparison> comparisons;
  
  /** should only be constructed from factory */
  Character() {
    // automatically add date was constructed
  }

  // obo edit annotation that gets modified in setValues - this alternative was passed
  // for just having a whole separate AnnotationCharacter
  // private OBOEditAnntotation - get & set methods


  /** for generic fields its just a map from char field to char field value */
  public void setValue(CharField cf, CharFieldValue cfv) {
    cfv.setCharacter(this);
    // not a list or empty list or list with empty value
//     if (!cf.isList() || !hasValue(cf)) {
//       List<CharFieldValue> newItem = new ArrayList<CharFieldValue>();
//       newItem.add(cfv);
    charFieldToValue.put(cf,cfv); //newItem);
//     }
//     else {
//       List<CharFieldValue> list = charFieldToValue.get(cf);
//       list.add(cfv);
//     }
    //charFieldToValue.put(cf,cfv);
    //System.out.println("Char setVal "+cf+" val "+cfv);
    // setOboEditModel(oboEditAnnotation,cf,cfv);
  }



  /** generic getter - getting single value or 1st item of list */ 
  public CharFieldValue getValue(CharField cf) {
    CharFieldValue cfv = charFieldToValue.get(cf);
    // undo needs to be able to undo back to null/""/init somehow
    if (cfv == null) {
      cfv = CharFieldValue.emptyValue(this,cf); // set in hash?
      //if (cf.isList()) cfv.setIsList(true); // emptyVal now does this
    }
    return cfv;
    //List<CharFieldValue> cfvList = charFieldToValue.get(cf);
    
    // if cfv is null should we construct a CFV w null/"", should field be init to ""?
    // undo needs to be able to undo back to null/""/init somehow
//     if (cfvList == null || cfvList.isEmpty()) {
//       CharFieldValue empty = CharFieldValue.emptyValue(this,cf);
//       List<CharFieldValue> list = new ArrayList<CharFieldValue>();
//       list.add(empty);
//       charFieldToValue.put(cf,list); // ??? prevents making new cfv every time
//       return empty;
//       //return CharFieldValue.emptyValue(this,cf); // set in hash-list?
//     }
//     return cfvList.get(0);
  }


  // return null if not set? empty list? ???
  public List<CharFieldValue> getValueList(CharField cf) {
    if (charFieldToValue.get(cf) == null) {
      return new ArrayList<CharFieldValue>();
    } else {
      return charFieldToValue.get(cf).getCharFieldValueList(); // ?? 
    }
  }

  /** returns string for charfield value of char field, no matter what type,
      return "" if null value?? throw ex? used for sorting CharList
      this returns names not ids for terms */
  public String getValueString(CharField cf) {
    if (!hasValue(cf)) return "";
    if (cf.isList()) {
      List<CharFieldValue> l = getValueList(cf);
      if (l == null || l.isEmpty()) return "";
      String s = l.get(0).getName();
      for (int i=1; i<l.size(); i++)
        s+= ", "+l.get(i).getName();
      return s;
    }
    CharFieldValue cfv = getValue(cf);
    if (cfv.getName() == null) return ""; // ?? ex?
    return cfv.getName();
  }

  /** throws exception if field doesnt exist */
  public String getValueString(String field) throws CharFieldException {
    CharField cf = getCharFieldForName(field); // throws ex
    return getValueString(cf);
    //if (!hasValue(cf)) return null; // ?? exception? ""?
    //return getValue(cf).getName();
  }


  // --> AbstractCharacter
//   public OBOClass getTerm(String field) throws CharFieldException {
//     CharField cf = getCharFieldForName(field); // throws ex
//     return getTerm(cf);
//   }

  /** return false if getValue is null or isEmpty */
  public boolean hasValue(CharField cf) {
    // empty string is a valid value for non-required field - or should there
    // be some sort of somthing to indicate "empty" value?
    //return getValue(cf) != null; // && !getValue(cf).equals("");
    if (getValue(cf) == null) return false;
    // getValue returns 1st item of list which is sufficent for isEmpty
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
    return charFieldMan().getCharFieldForName(fieldName);
  }

  /** return all char fields configured, both currently used or null in char */
  public List<CharField> getAllCharFields() {
    return charFieldMan().getCharFieldList();
  }

  private CharFieldManager charFieldMan() { return CharFieldManager.inst(); }

  public CharacterI cloneCharacter() {
    //try {
    // do OBOClasses clone? do we need to clone them? dont think so - immutable
    Character charClone = new Character(); //(Character)clone();

    // WRONG - will use same charFieldVal which points to old char!!
//    clone.charFieldToValue =(HashMap<CharField,CharFieldValue>)charFieldToValue.clone();
    // clone.setCharFieldValuesCharacterToSelf(); ???
    for (CharFieldValue v : charFieldToValue.values()) {
      // charClone actually set twice for cfv, cloneCFC & setValue, what the heck
      // sets kids characters as well
      CharFieldValue cfvClone = v.cloneValueForChar(charClone);
      charClone.setValue(cfvClone.getCharField(),cfvClone);
    }
    return charClone;

  }

  /** returns true if all non-auto-generated fields have no value. date_created is an
      example of an auto-generated field that gets set with character creation
      should this be renamed hasNoUserContent? */
  public boolean hasNoContent() {
    for (CharField cf : getAllCharFields()) {
      // if a field is auto generated (date_created) ignore it
      if (cf.isAutoGenerated()) continue;
      if (hasValue(cf)) return false;
    }
    return true;
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (CharField cf : getAllCharFields()) {
      if (hasValue(cf)) // take out nulls?
        sb.append(cf.getName() + ": " + charFieldToValue.get(cf) + CharField.LIST_DELIM + " ");
    }
    if (sb.length() > 0)
      sb.deleteCharAt(sb.length()-2); // lop off last comma
    // cut it down to reasonable size
    if (sb.length() > 100)
      return sb.substring(0,97) + "...";
    return sb.toString();
  }

  public boolean supportsComparisons() { return true; }

//   /** comparison actually goes in charFieldValue like another field */
//   public void addComparison(OBOProperty rel, CharacterI relChar) throws CharacterEx {
// //     String m = "Character datamodel does not implement comparisons, configure "
// //       +"datamodel to OBO Annotations to use comparisons";
// //     throw new CharacterEx(m);
//     // for now just allow 1 comparison
//     Comparison c = new Comparison(rel,relChar);
//     if (comparisons==null) comparisons = new ArrayList<Comparison>(2);
//     comparisons.add(c);
//     // do we add reverse comparison to relChar?? not sure
//     // relChar.
//     if (!charFieldMan().hasComparisonField()) { // shouldnt happen?
//       // ideally create one...
//       // Config/CharFieldMan.addCompField()
//       log().error("No Comparison field configged, cant add comparison");
//       return; // ex?
//     }
//     CharField cf = charFieldMan().getComparisonField();
//     CharFieldValue v = CharFieldValue.makeComparison(cf,this,rel,relChar);
//     setValue(cf,v); // transaction???
//   }

//   private class Comparison {
//     private CharacterI subject;
//     private OBOProperty rel;
//     private CharacterI object;
//     private Comparison(OBOProperty r,CharacterI c) { rel = r; object = c; }
//   }

  private Logger log;
  @SuppressWarnings("unused")
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }
}
  /** returns true if value(s) for char field are equal - if list - true if
      lists are identical */
//   public boolean fieldEquals(CharacterI c, CharField cf) {
//     List<CharFieldValue> list1 = getValueList(cf);
//     List<CharFieldValue> list2 = c.getValueList(cf);
//     if (list1 == null && list2 == null) return true;
//     if (list1 == null || list2 == null) return false;
//     if (list1.size() != list2.size()) return false;
//     // assumes lists are unique - and they should be
//     for (CharFieldValue cfv1 : list1) {
//       if (!list2.contains(cfv1)) // contains does .equals()
//         return false;
//     }
//     return true; // all char field values in list are equal
//   }
  // public void addValue ??? for lists??? is it funny that setVal is used for lists?

  // add to characterI - for lists - should this have a rank/order - is it possible
  // to have duplicates???
//   public void deleteValue(CharField cf, CharFieldValue cfv) {
//     if (!hasValue(cf)) return;
//     List<CharFieldValue> list = charFieldToValue.get(cf);
//     for (CharFieldValue v : list) {
//       if (v.equals(cfv)) {
//         list.remove(cfv);
//         return;
//       }
//     }
//   }

//   /** delete ith item from list */
//   public void deleteValue(CharField cf, int index) {
//     if (!hasValue(cf)) return;
//     List<CharFieldValue> list = charFieldToValue.get(cf);
//     list.remove(index);
//   }
