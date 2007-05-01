package phenote.gui.selection;

import java.util.EventObject;
import java.util.List;

import phenote.datamodel.CharacterI;

public class CharSelectionEvent extends EventObject {

  private List<CharacterI> charList;
  private List<CharacterI> previouslySelectedChars;

  CharSelectionEvent(Object source, List<CharacterI> chars, List<CharacterI> prev) {
    super(source);
    this.charList= chars;
    this.previouslySelectedChars = prev;
  }

  public List<CharacterI> getChars() { return charList; }

  public boolean isMultiSelect() { return charList.size() > 1; }

  public boolean hasPreviouslySelectedChars() {
    return previouslySelectedChars != null && !previouslySelectedChars.isEmpty();
  }

  public List<CharacterI> getPreviouslySelectedChars() { return previouslySelectedChars; }

}
