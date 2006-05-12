package phenote.dataadapter;

import java.util.EventObject;
import phenote.datamodel.CharacterListI;

public class CharListChangeEvent extends EventObject {

  private CharacterListI charList;

  CharListChangeEvent(Object source, CharacterListI charList) {
    super(source);
    this.charList = charList;
  }

  public CharacterListI getCharacterList() { return charList; }
}
