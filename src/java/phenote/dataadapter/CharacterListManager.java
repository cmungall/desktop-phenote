package phenote.dataadapter;
// I think this makes more sense in dataadapter than datamodel ?
// actually should go in edit package maybe?

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.CharFieldManager;

/** Contains current CharacterList. sends out CharacterListChanged events
    when character list has changed */
public class CharacterListManager {

  //private static CharacterListManager singleton;
  private static Map<String,CharacterListManager> groupToListMan =
    new HashMap<String,CharacterListManager>();

  private CharacterListI characterList = new CharacterList();
  private List<CharListChangeListener> listenerList = new ArrayList<CharListChangeListener>(6);
  private File currentFile; // can hold source of current character list if loaded from a file

//  public CharacterListManager() {}

  // default()?
  public static CharacterListManager main() {
    return getCharListMan(CharFieldManager.DEFAULT_GROUP);
  }

  /** Returns "default" CharacterListManager */ 
  public static CharacterListManager inst() {
    return main();
//     if (singleton == null)
//       singleton = new CharacterListManager();
//     return singleton;
  }

  public static CharacterListManager getCharListMan(String group) {
    if (group == null) group = CharFieldManager.DEFAULT_GROUP; // ??
    if (groupToListMan.get(group) == null) {
      CharacterListManager e = new CharacterListManager(); // (group) ?
      groupToListMan.put(group,e);
    }
    return groupToListMan.get(group);
  }

  public void setCharacterList(Object source, CharacterListI charList) {
    if (charList == null || charList.isEmpty()) {
      String m = "Data list is empty, load of data failed";
      // should this send message to gui?
      JOptionPane.showMessageDialog(null,m,"Load failure",JOptionPane.ERROR_MESSAGE);
      return;
    }
    characterList.getList().clear();
    characterList.getList().addAll(charList.getList());
    //characterList = charList;
    fireChangeEvent(source,charList);
  }

  public List<CharacterI> getCharList() { return characterList.getList(); }

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
  
  public File getCurrentDataFile() {
    return this.currentFile;
  }
  
  public void setCurrentDataFile(File aFile) {
    this.currentFile = aFile;
  }


}
