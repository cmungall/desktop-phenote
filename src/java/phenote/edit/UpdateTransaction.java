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

  //public void undo() { oldValue.setValue();//charFieldEnum.setValue(character,oldValue); }

}
