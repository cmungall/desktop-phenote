package phenote.datamodel;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

/** i made this as a transferable when i didnt get the whole transferable thing
    and now i dont whether to scrap it or not. all it is at the moment is a
    wrapper of a List<CharacterI> - but one could imagine this gaining some real
    functionality - i guess ill keep it for now */
public class CharacterList implements CharacterListI {

  private EventList<CharacterI> characterList = new BasicEventList<CharacterI>();

  

  public CharacterI get(int i) {
    // should this check if i is out of range - throw ex, or print error msg??
    return characterList.get(i);
  }
  public void add(CharacterI c) {
	characterList.add(c);  
  }
  public void add(int i, CharacterI c) {
	characterList.add(i,c); 
  }
  public void remove(int i) { characterList.remove(i); }
  public void remove(CharacterI c) { characterList.remove(c); }
  public void clear() { characterList.clear(); }
  public int size() { return characterList.size(); }
  public boolean isEmpty() { return characterList.isEmpty(); }
  public int indexOf(CharacterI c) { return characterList.indexOf(c); }
  public EventList<CharacterI> getList() { return characterList; }
   
  public boolean equals(CharacterListI cl) {
    if (size() != cl.size()) return false;
    for (int i=0; i<size(); i++) {
      if (!get(i).equals(cl.get(i)))
        return false;
    }
    return true;
  }

  /** return all characters that arent blanks. this is handy as blanks are really
      just an artifact of the gui. a character that has auto-generated fields filled
      in is still considered blank (like date_created). uses hasNoContent() */
  public EventList<CharacterI> getNonBlankList() {
    EventList<CharacterI> nonBlanks = new BasicEventList<CharacterI>(size());
    if (characterList == null) return nonBlanks; // can this happen?
    for (CharacterI c : characterList) {
      if (!c.hasNoContent()) // hasNoCont skips auto-gen fields
        nonBlanks.add(c);
    }
    return nonBlanks;
  }
  
  public CharacterListI getNonBlankCharList() {
    CharacterList l = new CharacterList();
    l.characterList = getNonBlankList();
    return l;
  }

}
