package phenote.edit;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharField;
//import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;

public class UpdateTransaction implements TransactionI { // extends Transaction?

//   private CharacterI character;
//   private CharFieldEnum charFieldEnum;
  //private String newValue; // OBOClass?
  //private String oldValue;
  private CharFieldValue newValue;
  private CharFieldValue oldValue;
  // not sure but this may be needed
  private boolean isUndone = false;
  //private boolean undo?

//   public UpdateTransaction(CharacterI c, CharFieldEnum e, String newVal) {
//     newValue = new CharFieldValue(newVal,c,e);
//     oldValue = e.getValue(c);
//   }

  public UpdateTransaction(CharacterI c, CharField cf, String newVal) {
    newValue = new CharFieldValue(newVal,c,cf);
    oldValue = c.getValue(cf);
  }

  // public UpdateTransaction(CharFieldValue new, CharFieldValue old) ?

  /** actually maybe dont need to pas in old val - just query for it phase out*/
//   public UpdateTransaction(CharacterI c,CharFieldEnum e,OBOClass newVal) {
//     newValue = new CharFieldValue(newVal,c,e);
//     //oldValue = new CharFieldValue(old,c,e);
//     oldValue = e.getValue(c);
//   }
  public UpdateTransaction(CharacterI c,CharField cf,OBOClass newVal) {
    newValue = new CharFieldValue(newVal,c,cf);
    oldValue = c.getValue(cf);
  }

  public boolean isUpdate() { return true; }

  // hmmmmmmm....
// /** if isDifferentia is true than update is for differentia of a post composed term*/
//   public UpdateTransaction(CharacterI c, CharFieldEnum e, boolean isDifferentia,
//                            OBOClass newVal, OBOClass old) {
//     newValue = new CharFieldValue(newVal,c,e,isDifferentia);
//     oldValue = new CharFieldValue(old,c,e,isDifferentia);

//   }

  public void editModel() {
    newValue.editModel();
    isUndone = false;
    //charFieldEnum.setValue(character,newValue);
  }

  /** return new value for regular trans, old value for undo? */
  //String getValue() { // if undo  } - does updateTrans know of undo??? not sure

  String getNewValueString() { return newValue.getName(); }

//   public boolean isUpdateForCharField(CharField cf) {
//     return getCharFieldEnum() == cf.getCharFieldEnum();    
//   }
  public boolean isUpdateForCharField(CharField cf) {
    // the char field should be the same shouldnt they? they better be?
    // test for null newVal?
    return newValue.getCharField().equals(cf);    
  }

//   private CharFieldEnum getCharFieldEnum() {
//     return newValue.getCharFieldEnum();
//   }

  public void undo() { 
    //oldValue.setValue();//charFieldEnum.setValue(character,oldValue); }
    oldValue.editModel();
    isUndone = true; // super.undo()?
  }

  public boolean isUndone() { return isUndone; }

  // public String getValue() { if isUndone return oldVal else newVal ??
  
}
