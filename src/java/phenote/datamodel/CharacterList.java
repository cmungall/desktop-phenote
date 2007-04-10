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
  public int sorted;
  public CharField cf_sorted = null;
  public static int UNSORTED = 0;  //constants to explain sorting
  public static int FORWARD_SORT = 1;
  public static int REV_SORT = 2;

  public CharacterI get(int i) { return characterList.get(i); }
  public void add(CharacterI c) {//once a new char is added, any sorts are voided
	characterList.add(c); 
	sorted=UNSORTED; 
  }
  public void add(int i, CharacterI c) {//once a new char is added, any sorts are voided
	characterList.add(i,c); 
	sorted=UNSORTED;
  }
  public void remove(int i) { characterList.remove(i); }
  public void remove(CharacterI c) { characterList.remove(c); }
  public int size() { return characterList.size(); }
  public int indexOf(CharacterI c) { return characterList.indexOf(c); }
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
  public void sortBy(CharField cf) {
	if (cf_sorted!=cf) {sorted=UNSORTED;}  //newly selected cf
	if ((sorted==UNSORTED) || (sorted==REV_SORT)) {
//	  Collections.sort(characterList,getComparator(cf));
	  Collections.sort(characterList,getForwardComparator(cf));
	  sorted = FORWARD_SORT;
//	  System.out.println("forward sort!");
	} else if ((sorted == FORWARD_SORT)) {
		sorted = REV_SORT;  
  	    Collections.sort(characterList,getReverseComparator(cf));
//		System.out.println("need to reverse sort!");
	}
	  cf_sorted = cf;  //remember the last col that was sorted
  }

//  private Comparator<CharacterI> getComparator(final CharField cf) {
//    return new Comparator<CharacterI>() {
//      public int compare(CharacterI c1, CharacterI c2) {
//        String s1 = c1.getValueString(cf);//cf.getValue(c1).getName();
//        String s2 = c2.getValueString(cf);
//        return s1.compareTo(s2);
//      }
//    };
//  }
  
  private Comparator<CharacterI> getForwardComparator(final CharField cf) {
	    return new Comparator<CharacterI>() {
	      public int compare(CharacterI c1, CharacterI c2) {
	        String s1 = c1.getValueString(cf);//cf.getValue(c1).getName();
	        String s2 = c2.getValueString(cf);
	        return s1.compareToIgnoreCase(s2);
	      }
	    };
	  }

  private Comparator<CharacterI> getReverseComparator(final CharField cf) {
	    return new Comparator<CharacterI>() {
	      public int compare(CharacterI c1, CharacterI c2) {
	        String s1 = c1.getValueString(cf);//cf.getValue(c1).getName();
	        String s2 = c2.getValueString(cf);
	        return s2.compareToIgnoreCase(s1);
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
