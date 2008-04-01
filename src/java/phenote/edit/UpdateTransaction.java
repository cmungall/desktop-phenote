package phenote.edit;

import java.util.ArrayList;
import java.util.List;

import org.obo.datamodel.OBOClass;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.Comparison;

public class UpdateTransaction implements TransactionI { // extends Transaction?

  private CharFieldValue newValue;
  private CharFieldValue oldValue;
  // not sure but this may be needed
  private boolean isUndone = false;

  public UpdateTransaction(CharacterI c, CharField cf, String newString) {
//     newValue = new CharFieldValue(newVal,c,cf);
//     setOldValue(c,cf);
    oldValue = c.getValue(cf);
    // if list adds to list, if not just sets it, in either case makes new object
    newValue = CharFieldValue.makeNewValue(newString,oldValue);
  }

  public UpdateTransaction(CharacterI c,CharField cf,OBOClass newTerm) {
    oldValue = c.getValue(cf);
    // if list adds to list, if not just sets it, in either case makes new object
    newValue = CharFieldValue.makeNewValue(newTerm,oldValue);//new CharFieldValue(newVal,c,cf);
  }
  
  /** used by character list field gui in character template, v is new value
      also used by comparisons
      deals with lists via CFV.makeNewValue
      im not sure how this works as makeNewValue seems funny */
  public UpdateTransaction(CharacterI c, CharField cf, CharFieldValue v) {
    oldValue = c.getValue(cf);
    newValue = CharFieldValue.makeNewValue(v, oldValue);
  }

  public UpdateTransaction(CharFieldValue oldVal, CharFieldValue newVal) {
    oldValue = oldVal;
    newValue = newVal;
  }

  /** add comparison to potentially a list of comparisons in subject comparison
      char field */
  static UpdateTransaction addComparison(Comparison comparison) {
    CharFieldValue newKid = new CharFieldValue(comparison);
    CharacterI sub = comparison.getSubject();
    CharField cf = newKid.getCharField();

    CharFieldValue oldParent = sub.getValue(cf);
    CharFieldValue newParent = oldParent.cloneCharFieldValue();
    newParent.addKid(newKid);

    return new UpdateTransaction(oldParent,newParent);
    // old value really depends on whats allowed in gui - for now assume 
    // 1 comp per char but this will need updating
    // for now just use old comparison of subject - this means only 1 comparison per
    // character - for now - improve with better gui
//     if (comparison.hasSubject()) { // it should have subject!
//       CharField f = CharFieldManager.inst().getComparisonField();
//       oldValue = comparison.getSubject().getValue(f); // might be empty/null(?)
//     }
    //oldValue = comparison.getValue(); // could be null, could be a list of comps
  }

//   /** delete comparison from list of comparisons, comparisons are always in lists */
//   static UpdateTransaction deleteComparison(Comparison oldComp) {
//     CharFieldValue deletedComp
//   }

  protected void setOldValue(CharacterI c, CharField cf) {
    // if list then there is no old value (as its really an add - cheesy?)
    if (cf.isList()) oldValue = CharFieldValue.emptyValue(c,cf);
    else oldValue = c.getValue(cf);
  }

  public List<CharacterI> getDeletedAnnotations() {
    return new ArrayList<CharacterI>(0);
  }


  public List<CharacterI> getCharacters() {
    List<CharacterI> l = new ArrayList<CharacterI>();
    if (newValue.getCharacter()!=null) l.add(newValue.getCharacter());
    return l;
  }

  public boolean isAdd() { return false; }

  public boolean isUpdate() { return true; }

  public void editModel() {
    // hmmm??? - DELETE really - cheap to do through update? cant call edit model
    // on old or new as new doesnt know old to del, old doesnt know its del
    // this was hacky way when char had list of vals - no longer needed
//     if (newValue.isEmpty() && newValue.getCharField().isList())
//       oldValue.editModelDelete(); // ???
//     else
    newValue.editModel();
    isUndone = false;
    //charFieldEnum.setValue(character,newValue);
  }

  public String getNewValueString() { return "blah blah blah";}
  
  public OBOClass getNewTerm() { 
	if (newValue.isTerm())
	    return newValue.getTerm(); 
	else return null; 
  }

  public boolean isUpdateForCharField(CharField cf) {
    // the char field should be the same shouldnt they? they better be?
    // test for null newVal?
    return newValue.getCharField().equals(cf);    
  }


  public void undo() { 
    oldValue.editModel();
    isUndone = true; // super.undo()?
  }

  public boolean isUndone() { return isUndone; }
  
}


//   private CharacterI character;
//   private CharFieldEnum charFieldEnum;
  //private String newValue; // OBOClass?
  //private String oldValue;
//   public UpdateTransaction(CharacterI c, CharFieldEnum e, String newVal) {
//     newValue = new CharFieldValue(newVal,c,e);
//     oldValue = e.getValue(c);
//   }

  // hmmmmmmm....
// /** if isDifferentia is true than update is for differentia of a post composed term*/
//   public UpdateTransaction(CharacterI c, CharFieldEnum e, boolean isDifferentia,
//                            OBOClass newVal, OBOClass old) {
//     newValue = new CharFieldValue(newVal,c,e,isDifferentia);
//     oldValue = new CharFieldValue(old,c,e,isDifferentia);

//   }
  /** return new value for regular trans, old value for undo? */
  //String getValue() { // if undo  } - does updateTrans know of undo??? not sure
//   public boolean isUpdateForCharField(CharField cf) {
//     return getCharFieldEnum() == cf.getCharFieldEnum();    
//   }
//   private CharFieldEnum getCharFieldEnum() {
//     return newValue.getCharFieldEnum();
//   }
    //oldValue.setValue();//charFieldEnum.setValue(character,oldValue); }
  // public String getValue() { if isUndone return oldVal else newVal ??
  // public UpdateTransaction(CharFieldValue new, CharFieldValue old) ?

  /** actually maybe dont need to pas in old val - just query for it phase out*/
//   public UpdateTransaction(CharacterI c,CharFieldEnum e,OBOClass newVal) {
//     newValue = new CharFieldValue(newVal,c,e);
//     //oldValue = new CharFieldValue(old,c,e);
//     oldValue = e.getValue(c);
//   }
