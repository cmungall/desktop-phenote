package phenote.edit;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
// does char list manager belong in edit package?
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;

/** For adding a character (not a char subpart - thats an update) */

public class AddTransaction implements TransactionI {

  private CharacterI addChar;

  public AddTransaction(CharacterI chr) {
    addChar = chr;
  }

  public boolean isUpdate() { return false; }

  public void editModel() {
    getCharList().add(addChar);
  }

  public void undo() { getCharList().remove(addChar); }

  // should char list be passed in? charlistMan part of edit pkg?
  private CharacterListI getCharList() {
    return CharacterListManager.inst().getCharacterList();
  }

  /** this is for update trans - return false */
  public boolean isUpdateForCharField(CharField cf) { return false; }

  public OBOClass getNewTerm() { return null; }

}
