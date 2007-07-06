package phenote.dataadapter;
// I think this makes more sense in dataadapter than datamodel ?
// actually should go in edit package maybe?

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;

/** Contains current CharacterList. sends out CharacterListChanged events
    when character list has changed */
public class CharacterListManager {

  private static CharacterListManager singleton;

  private CharacterListI characterList = new CharacterList();
  private List<CharListChangeListener> listenerList = new ArrayList<CharListChangeListener>(6);

  public CharacterListManager() {}

  public static CharacterListManager inst() {
    if (singleton == null)
      singleton = new CharacterListManager();
    return singleton;
  }

  public void setCharacterList(Object source, CharacterListI charList) {
    if (charList == null || charList.isEmpty()) {
      String m = "Data list is empty, load of data failed";
      // should this send message to gui?
      JOptionPane.showMessageDialog(null,m,"Load failure",JOptionPane.ERROR_MESSAGE);
      return;
    }
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

  public void clear() {
    characterList.clear();
    // notify listeners???
  }

}
