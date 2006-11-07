package phenote.gui.selection;

import java.util.EventObject;
import java.util.List;

import phenote.datamodel.CharacterI;

public class CharSelectionEvent extends EventObject {

  private List<CharacterI> charList;

  CharSelectionEvent(Object source, List<CharacterI> chars) {
    super(source);
    this.charList= chars;
  }

  public List<CharacterI> getChars() { return charList; }

  public boolean isMultiSelect() { return charList.size() > 1; }

}
