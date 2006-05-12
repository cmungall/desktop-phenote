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
  // List<CharacterSelectionListener> characterListenerList;
  private CharacterI selectedCharacter;

  public static SelectionManager inst() {
    if (singleton == null) singleton = new SelectionManager();
    return singleton;
  }

  private SelectionManager() {
    termListenerList = new ArrayList<TermSelectionListener>(5);
  }

  public void addTermSelectionListener(TermSelectionListener l) {
    termListenerList.add(l);
  }

  // void addCharacterSelectionListener(CharacterSelectionListener l) {}

  public void selectTerm(Object source, OBOClass oboClass) {
    TermSelectionEvent e = makeTermEvent(source,oboClass);
    Iterator<TermSelectionListener> it = termListenerList.iterator();
    while(it.hasNext())
      it.next().termSelected(e);
  }

  // void selectTerm(String termName) {} ???

  private TermSelectionEvent makeTermEvent(Object src, OBOClass oc) {
    return new TermSelectionEvent(src,oc);
  }

  public CharacterI getSelectedCharacter() {
    return selectedCharacter;
  }

  public void selectCharacter(Object source, CharacterI character) {
    selectedCharacter = character;
    // CharacterSelectionEvent e = makeCharacterEvent(source,character);
    // for (CharacterSelectionListener l : characterListenerList)
    // l.characterSelected(e);
  }

}
