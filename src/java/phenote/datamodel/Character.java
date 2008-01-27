package phenote.datamodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

import org.obo.datamodel.OBOClass;

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
  
  /** should only be constrcuted from factory */
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
      if (cf.isList()) cfv.setIsList(true);
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
      return "" if null value?? throw ex? used for sorting CharList */
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
    return CharFieldManager.inst().getCharFieldForName(fieldName);
  }

  /** return all char fields configured, both currently used or null in char */
  public List<CharField> getAllCharFields() {
    return CharFieldManager.inst().getCharFieldList();
  }


  public CharacterI cloneCharacter() {
    //try {
    // do OBOClasses clone? do we need to clone them? dont think so - immutable
    Character charClone = new Character(); //(Character)clone();

    // WRONG - will use same charFieldVal which points to old char!!
//    clone.charFieldToValue =(HashMap<CharField,CharFieldValue>)charFieldToValue.clone();
    // clone.setCharFieldValuesCharacterToSelf(); ???
    for (CharFieldValue v : charFieldToValue.values()) {
      CharFieldValue cfvClone = v.cloneCharFieldValue();
      // cfvClone.setCharacter(charClone); done by setValue
      charClone.setValue(cfvClone.getCharField(),cfvClone);
    }
//     for (List<CharFieldValue> list : charFieldToValue.values()) {
//       for (CharFieldValue v : list) {
//         CharFieldValue cfvClone = v.cloneCharFieldValue();
//         // cfvClone.setCharacter(charClone); done by setValue
//         charClone.setValue(cfvClone.getCharField(),cfvClone);
//       }
//     }
    return charClone;

  }

  // used by character table panel
  public boolean hasNoContent() {
    for (CharField cf : getAllCharFields()) {
      if (hasValue(cf)) return false;
    }
    return true;
  }
 
  public String toString() {
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
