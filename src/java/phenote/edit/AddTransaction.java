package phenote.edit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obo.datamodel.OBOClass;

import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;

/** For adding a character (not a char field/subpart - thats an update) */

public class AddTransaction implements TransactionI {

  private CharacterI addChar;
  private CharacterListManager characterListManager;
  private boolean isUndone = false;
  // private int index; ??

  public AddTransaction(CharacterI chr, CharacterListManager clManager) {
    this.addChar = chr;
    this.characterListManager = clManager;
  }

  public List<CharacterI> getDeletedAnnotations() {
    return new ArrayList<CharacterI>(0);
  }

  public boolean isUpdate() { return false; }
  public boolean isAdd() { return !isUndone; }

  public void editModel() {
    getCharList().add(addChar);
    isUndone = false;
    // automatically put in date_created???  //try {
    setDateCreated();
    // if has auto id field then, create annot id
    setAutoAnnotId();
    // index = getCharList().indexOf(addChar); ??
  }

  private void setDateCreated() {
    CharField dateField = CharFieldManager.inst().getDateCreatedField();
    CharFieldValue v = new CharFieldValue(getDate(),addChar,dateField);
    addChar.setValue(dateField,v);
    //} catch (CharFieldException e) { System.out.println("no date_created field"); } //?
  }

  /** if char has an auto annot id then set it */
  private void setAutoAnnotId() {
    if (!CharFieldManager.inst().hasAutoAnnotField())
      return;
    CharField idField = CharFieldManager.inst().getAutoAnnotField();
    String id = AutoAnnotIdGenerator.getNewId();
    CharFieldValue idVal = new CharFieldValue(id,addChar,idField);
    addChar.setValue(idField,idVal);
  }

  // get current date
  private Date getDate() { return new Date(); }

// public int getIndex() { return index; } ???

  public void undo() {
    getCharList().remove(addChar);
    isUndone = true;
  }

  public List<CharacterI> getCharacters() {
    List<CharacterI> l = new ArrayList<CharacterI>();
    l.add(addChar);
    return l;
  }

  // should char list be passed in? charlistMan part of edit pkg?
  private CharacterListI getCharList() {
    return this.characterListManager.getCharacterList();
  }

  /** this is for update trans - return false */
  public boolean isUpdateForCharField(CharField cf) { return false; }

  public OBOClass getNewTerm() { return null; }

}
