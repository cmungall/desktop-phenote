package phenote.edit;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;

public class UpdateTransaction implements TransactionI { // extends Transaction?

//   private CharacterI character;
//   private CharFieldEnum charFieldEnum;
  //private String newValue; // OBOClass?
  //private String oldValue;
  private CharFieldValue newValue;
  private CharFieldValue oldValue;
  //private boolean undo?

  public UpdateTransaction(CharacterI c, CharFieldEnum e, String newVal) {
    newValue = new CharFieldValue(newVal,c,e);
    // change this to get old from model - dont need to pass in
    //oldValue = new CharFieldValue(old,c,e);
    oldValue = e.getValue(c);
//     character = c;
//     charFieldEnum = e;2
//     newValue = newVal;
//     oldValue = old;
  }

  // hmmmm - should this be a compos - yes
  //public UpdateTransaction(List<CharacterI>chars,CharFieldEnum e,String newVal) {}


  // public UpdateTransaction(CharFieldValue new, CharFieldValue old) ?

  /** actually maybe dont need to pas in old val - just query for it */
  public UpdateTransaction(CharacterI c,CharFieldEnum e,OBOClass newVal) {
    newValue = new CharFieldValue(newVal,c,e);
    //oldValue = new CharFieldValue(old,c,e);
    oldValue = e.getValue(c);
  }


  // hmmmmmmm....
// /** if isDifferentia is true than update is for differentia of a post composed term*/
//   public UpdateTransaction(CharacterI c, CharFieldEnum e, boolean isDifferentia,
//                            OBOClass newVal, OBOClass old) {
//     newValue = new CharFieldValue(newVal,c,e,isDifferentia);
//     oldValue = new CharFieldValue(old,c,e,isDifferentia);

//   }

  public void editModel() {
    newValue.editModel();
    //charFieldEnum.setValue(character,newValue);
  }

  /** return new value for regular trans, old value for undo? */
  //String getValue() { // if undo  } - does updateTrans know of undo??? not sure

  String getNewValueString() { return newValue.getName(); }

  CharFieldEnum getCharFieldEnum() {
    return newValue.getCharFieldEnum();
  }

  //public void undo() { oldValue.setValue();//charFieldEnum.setValue(character,oldValue); }

}
