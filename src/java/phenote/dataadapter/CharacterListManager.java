package phenote.dataadapter;
// I think this makes more sense in dataadapter than datamodel ?

import java.util.ArrayList;
import java.util.List;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharacterList;

/** Contains current CharacterList. sends out CharacterListChanged events
    when character list has changed */
public class CharacterListManager {

  private static CharacterListManager singleton;

  private CharacterListI characterList = new CharacterList();
  private List<CharListChangeListener>listenerList
  = new ArrayList<CharListChangeListener>(6);

  private CharacterListManager() {}

  public static CharacterListManager inst() {
    if (singleton == null)
      singleton = new CharacterListManager();
    return singleton;
  }

  public void setCharacterList(Object source, CharacterListI charList) {
    characterList = charList;
    fireChangeEvent(source,charList);
  }

  public CharacterListI getCharacterList() { return characterList; }

  private void fireChangeEvent(Object source, CharacterListI charList) {
    CharListChangeEvent e = new CharListChangeEvent(source,charList);
    for (CharListChangeListener l : listenerList)
      l.newCharList(e);
  }

  public void addCharListChangeListener(CharListChangeListener l) {
    listenerList.add(l);
  }

}
