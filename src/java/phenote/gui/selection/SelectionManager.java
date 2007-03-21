package phenote.gui.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.geneontology.oboedit.datamodel.OBOClass;

import phenote.datamodel.CharacterI;

/** Controller for term & character selection */
public class SelectionManager {

  private static SelectionManager singleton;

  private List<TermSelectionListener> termListenerList;
  List<CharSelectionListener> charListenerList;
  // phase out for list...
  private CharacterI selectedCharacter;
  private List<CharacterI> selectedCharList;

  public static SelectionManager inst() {
    if (singleton == null) singleton = new SelectionManager();
    return singleton;
  }

  private SelectionManager() {
    termListenerList = new ArrayList<TermSelectionListener>(5);
    charListenerList = new ArrayList<CharSelectionListener>(4);
  }

  public void addTermSelectionListener(TermSelectionListener l) {
    termListenerList.add(l);
  }

  // void addCharacterSelectionListener(CharacterSelectionListener l) {}

  public void selectHistoryTerm(Object source, OBOClass oboClass, UseTermListener l) {
	  boolean isMouseOver = false;
	  boolean isHyperlink = true;
	  TermSelectionEvent e = makeTermEvent(source, oboClass, l, isMouseOver, isHyperlink);
	  fireSelect(e);
  }
  
  public void selectMouseOverTerm(Object source, OBOClass oboClass,UseTermListener l) {
	boolean isMouseOver = true;
	boolean isHyperlink = false;
    TermSelectionEvent e = makeTermEvent(source,oboClass,l,isMouseOver,isHyperlink);
    fireSelect(e);
  }
  
  public void selectTerm(Object source,OBOClass oboClass, boolean isHyperlink) {
	boolean isMouseOver = false;
//	System.out.println("ishyperlink="+isHyperlink);
	TermSelectionEvent e = makeTermEvent(source,oboClass,null,isMouseOver, isHyperlink);
    if (!isHyperlink) fireSelect(e);
  }

  private void fireSelect(TermSelectionEvent e) {
    Iterator<TermSelectionListener> it = termListenerList.iterator();
    while(it.hasNext())
      it.next().termSelected(e);   
  }
  
  
  // void selectTerm(String termName) {} ???

  private TermSelectionEvent makeTermEvent(Object src, OBOClass oc,UseTermListener l,boolean mouse, boolean link) {
    return new TermSelectionEvent(src,oc,l,mouse,link);
  }

  public CharacterI getFirstSelectedCharacter() {
    if (selectedCharList == null || selectedCharList.isEmpty())
      return null; // ex?
    return selectedCharList.get(0);
  }

  public List<CharacterI> getSelectedChars() {
    return selectedCharList;
  }

//   public void selectCharacter(Object source, CharacterI character) {
//     selectedCharacter = character;
//     CharSelectionEvent e = makeCharacterEvent(source,character);
//     for (CharSelectionListener l : charListenerList)
//       l.characterSelected(e);
//   }

  public void selectCharacters(Object src, List<CharacterI> chars) {
    selectedCharList = chars;
    //if (chars.size() == 1) { selectCharacter(src,chars.get(0)); return; }
    CharSelectionEvent e = new CharSelectionEvent(src,chars);
    for (CharSelectionListener l : charListenerList)
      l.charactersSelected(e);
  }
 

//   private CharSelectionEvent makeCharacterEvent(Object src, CharacterI c) {
//     return new CharSelectionEvent(src,c);
//   }

  public void addCharSelectionListener(CharSelectionListener l) {
    charListenerList.add(l);
  }

}
