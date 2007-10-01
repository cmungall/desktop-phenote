package phenote.edit;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.OntologyManager;

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
    // automatically put in date_created???
    try {
      CharField dateField = OntologyManager.inst().getCharFieldForName("date_created");
      CharFieldValue v = new CharFieldValue(getDate(),addChar,dateField);
    } catch (CharFieldException e) { System.out.println("no date_created field"); } //?
    isUndone = false;
    // index = getCharList().indexOf(addChar); ??
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
