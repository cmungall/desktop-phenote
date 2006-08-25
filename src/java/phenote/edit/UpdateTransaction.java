package phenote.edit;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharField.CharFieldEnum;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;

public class UpdateTransaction { // extends Transaction?

//   private CharacterI character;
//   private CharFieldEnum charFieldEnum;
  //private String newValue; // OBOClass?
  //private String oldValue;
  private CharFieldValue newValue;
  private CharFieldValue oldValue;
  //private boolean undo?

  public UpdateTransaction(CharacterI c, CharFieldEnum e, String newVal, String old) {
    newValue = new CharFieldValue(newVal,c,e);
    oldValue = new CharFieldValue(old,c,e);
//     character = c;
//     charFieldEnum = e;
//     newValue = newVal;
//     oldValue = old;
  }

  // public UpdateTransaction(CharFieldValue new, CharFieldValue old) ?

  public UpdateTransaction(CharacterI c, CharFieldEnum e, OBOClass newVal, OBOClass old) {
    newValue = new CharFieldValue(newVal,c,e);
    oldValue = new CharFieldValue(old,c,e);
  }

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
