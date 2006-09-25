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
  private CharacterI selectedCharacter;

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

  public void selectTerm(Object source, OBOClass oboClass,UseTermListener l) {
    TermSelectionEvent e = makeTermEvent(source,oboClass,l);
    Iterator<TermSelectionListener> it = termListenerList.iterator();
    while(it.hasNext())
      it.next().termSelected(e);
  }

  // void selectTerm(String termName) {} ???

  private TermSelectionEvent makeTermEvent(Object src, OBOClass oc,UseTermListener l) {
    return new TermSelectionEvent(src,oc,l);
  }

  public CharacterI getSelectedCharacter() {
    return selectedCharacter;
  }

  public void selectCharacter(Object source, CharacterI character) {
    selectedCharacter = character;
    CharSelectionEvent e = makeCharacterEvent(source,character);
    for (CharSelectionListener l : charListenerList)
      l.characterSelected(e);
  }

  private CharSelectionEvent makeCharacterEvent(Object src, CharacterI c) {
    return new CharSelectionEvent(src,c);
  }

  public void addCharSelectionListener(CharSelectionListener l) {
    charListenerList.add(l);
  }

}
