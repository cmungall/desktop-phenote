package phenote.edit;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;

class DeleteTransaction implements TransactionI {

  private CharacterI delChar;

  DeleteTransaction(CharacterI c) { delChar = c; }

  public void editModel() {
    getCharList().remove(delChar);
  }

  public void undo() { getCharList().add(delChar); }

  public boolean isUpdate() { return false; }

  public boolean isUpdateForCharField(CharField c) { return false; }

  // should char list be passed in? charlistMan part of edit pkg?
  private CharacterListI getCharList() {
    return CharacterListManager.inst().getCharacterList();
  }
    

}
