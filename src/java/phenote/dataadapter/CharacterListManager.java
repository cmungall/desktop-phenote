package phenote.dataadapter;
// I think this makes more sense in dataadapter than datamodel ?
// actually should go in edit package maybe?

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharFieldValue;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterList;
import phenote.datamodel.CharacterListI;
import phenote.datamodel.Comparison;

/** Contains current CharacterList. sends out CharacterListChanged events
    when character list has changed */
public class CharacterListManager {

  private static Map<String,CharacterListManager> groupToListMan =
    new HashMap<String,CharacterListManager>();

  private CharacterListI characterList = new CharacterList();
  private List<CharListChangeListener> listenerList = new ArrayList<CharListChangeListener>(6);
  private File currentFile; // can hold source of current character list if loaded from a file
  
  private CharacterListManager() {
    super();
  }

  public static CharacterListManager main() {
    return getCharListMan(CharFieldManager.DEFAULT_GROUP);
  }

  /** Returns "default" CharacterListManager */ 
  public static CharacterListManager inst() {
    return main();
  }
  
  public static void reset() {
    groupToListMan.clear();
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
    if (charList == null) {
      String m = "Data list is empty, load of data failed";
      // should this send message to gui?
      JOptionPane.showMessageDialog(null,m,"Load failure",JOptionPane.ERROR_MESSAGE);
      return;
    }
    characterList = charList;
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

  /** Return all comparisons. go through all characters and extract their comparisons
      if they have them. returns empty list if there are no comparisons. */
  public List<Comparison> getComparisons() {
    List<Comparison> l = new ArrayList<Comparison>();
    for (CharacterI c : getCharList())
      if (c.hasComparison()) l.addAll(c.getComparisons());
    return l;
  }
  /** Returns all kid CharFieldValue comparisons. there are a list of comparisons
      for each character. Theres a CharFieldValue parent that is the list of kid 
      CharFieldValues, and then theres the kid char field values that actually contain
      the Comparisons. that is what is returned here. refactor? */
  public List<CharFieldValue> getComparisonValues() {
    List<CharFieldValue> l = new ArrayList<CharFieldValue>();
    for (CharacterI c : getCharList())
      if (c.hasComparison()) l.addAll(c.getComparisonValueKidList());
    return l;
  }
}
