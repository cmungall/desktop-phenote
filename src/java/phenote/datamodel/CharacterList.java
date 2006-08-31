package phenote.datamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

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

  // ??
  public void sortBy(CharFieldEnum cfe) {
    Collections.sort(characterList,getComparator(cfe));
  }

  private Comparator<CharacterI> getComparator(final CharFieldEnum cfe) {
    return new Comparator<CharacterI>() {
      public int compare(CharacterI c1, CharacterI c2) {
        String s1 = cfe.getValue(c1).getName();
        String s2 = cfe.getValue(c2).getName();
        return s1.compareTo(s2);
      }
    };
  }
}

//   private class CharFieldComparator<CharacterI> implements Comparator {
//     private CharFieldEnum charFieldEnum;
//     private CharFieldComparator(CharFieldEnum cfe) {
//       charFieldEnum = cfe;
//     }
//     public int compare(CharacterI c1, CharacterI c2) {
//       String s1 = charFieldEnum.getValue(c1).getName();
//       String s2 = charFieldEnum.getValue(c2).getName();
//       return s1.compareTo(s2);
//     }
//   }   


//      Comparator<String> comparator = new Comparator<String>() {
//         public int compare(String s1, String s2) { // Ignore null for brevity
//             return -s1.compareTo(s2);
//         }
//      };
