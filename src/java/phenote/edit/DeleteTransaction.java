package phenote.edit;

import java.util.ArrayList;
import java.util.List;

import org.obo.datamodel.OBOClass;

import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;

class DeleteTransaction implements TransactionI {

  private CharacterI delChar;
  // remember where the char was in the list for undo
  private int listOrder;
  private CharacterListManager characterListManager;
  private boolean isUndone = false;
  private boolean isWholeAnnotationDelete = true;

  /** this is for whole row deletes? not del from list field? */
  public List<CharacterI> getDeletedAnnotations() {
    List<CharacterI> l = new ArrayList<CharacterI>();
    if (isWholeAnnotationDelete)
      l.add(delChar);
    return l;
  }

  DeleteTransaction(CharacterI c, CharacterListManager clManager) {
    isWholeAnnotationDelete = true;
    this.delChar = c;
    this.characterListManager = clManager;
  }

//   /** should deleting an item from a char field value list be a DeleteTrans
//       or an UpdateTrans? i think delete but not sure - os should delete
//       only be for deleting whole annots? move to separate FieldListItemDeleteTrans class?  */
//   public DeleteTransaction(CharacterI c, CharacterListManager cm, CharField cf,
//                            int index) {
//     isWholeAnnotationDelete = false;
//   }

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
    return this.characterListManager.getCharacterList();
  }
    
  public OBOClass getNewTerm() { return null; }

}
