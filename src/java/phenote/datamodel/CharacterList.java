package phenote.datamodel;

import java.util.ArrayList;
import java.util.List;

/** i made this as a transferable when i didnt get the whole transferable thing
    and now i dont whether to scrap it or not. all it is at the moment is a
    wrapper of a List<CharacterI> - but one could imagine this gaining some real
    functionality - i guess ill keep it for now */
public class CharacterList implements CharacterListI {

  private List<CharacterI> characterList = new ArrayList<CharacterI>();

  public CharacterI get(int i) { return characterList.get(i); }
  public void add(CharacterI c) { characterList.add(c); }
  public void remove(int i) { characterList.remove(i); }
  public int size() { return characterList.size(); }
  public List<CharacterI> getList() { return characterList; }

  public boolean equals(CharacterListI cl) {
    if (size() != cl.size()) return false;
    for (int i=0; i<size(); i++) {
      if (!get(i).equals(cl.get(i)))
        return false;
    }
    return true;
  }
}
