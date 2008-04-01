package phenote.dataadapter;

import java.util.EventObject;

import phenote.datamodel.CharacterListI;

/** this is for dataloading - contains the newly loaded char list - not necasarily
    for changes to char list hmm.... */

public class CharListChangeEvent extends EventObject {

  private CharacterListI charList;

  CharListChangeEvent(Object source, CharacterListI charList) {
    super(source);
    this.charList = charList;
  }

  public CharacterListI getCharacterList() { return charList; }
}
