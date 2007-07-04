package phenote.edit;

import java.util.ArrayList;
import java.util.List;
import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;

class DeleteTransaction implements TransactionI {

  private CharacterI delChar;
  // remember where the char was in the list for undo
  private int listOrder;
  private boolean isUndone = false;

  DeleteTransaction(CharacterI c) { delChar = c; }

  public void editModel() {
    listOrder = getCharList().indexOf(delChar);
    getCharList().remove(delChar);
    isUndone = false;
  }

  // this doesnt put it where it came from on bulk delete as its getting reentered in
  // opposite order it got deleted - maybe compound trans should be aware and do kids
  // in reverse on undo? - actually thats general
  public void undo() {
    getCharList().add(listOrder,delChar);
    isUndone = true;
  }

  public List<CharacterI> getCharacters() {
    List<CharacterI> l = new ArrayList<CharacterI>();
    if (delChar!=null) l.add(delChar);
    return l;
  }

  public boolean isAdd() { return isUndone; }

  public boolean isUpdate() { return false; }

  public boolean isUpdateForCharField(CharField c) { return false; }

  // should char list be passed in? charlistMan part of edit pkg?
  private CharacterListI getCharList() {
    return CharacterListManager.inst().getCharacterList();
  }
    
  public OBOClass getNewTerm() { return null; }

}
