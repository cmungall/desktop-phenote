package phenote.gui.selection;

import java.util.EventObject;

import phenote.datamodel.CharacterI;

public class CharSelectionEvent extends EventObject {

  private CharacterI character;

  CharSelectionEvent(Object source, CharacterI ch) {
    super(source);
    this.character= ch;
  }

  public CharacterI getCharacter() { return character; }

}