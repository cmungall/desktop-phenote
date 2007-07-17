package phenote.charactertemplate;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListSelectionModel;

import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharacterI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
import phenote.gui.selection.CharSelectionEvent;
import phenote.gui.selection.CharSelectionListener;
import phenote.gui.selection.SelectionManager;

/**
 * List selection model which stays in sync with SelectionManager.
 * Also automatically selects newly inserted characters.
 * This list selection model does not support non-contiguous selection.
 */
@SuppressWarnings("serial")
public class SelectionManagerListSelectionModel extends DefaultListSelectionModel implements CharChangeListener, CharSelectionListener {

  private CharacterListManager characterListManager;
  private EditManager editManager;
  private SelectionManager selectionManager;
  
  public SelectionManagerListSelectionModel(CharacterListManager clManager, EditManager eManager, SelectionManager selManager) {
    super();
    this.characterListManager = clManager;
    this.editManager = eManager;
    this.editManager.addCharChangeListener(this);
    this.selectionManager = selManager;
    this.selectionManager.addCharSelectionListener(this);
    this.setSelectionInterval(this.selectionManager.getSelectedChars());
  }

  public void charChanged(CharChangeEvent e) {
    if (e.isAdd()) {
      this.setSelectionInterval(e.getTransaction().getCharacters());
    } 
  }

  public void setSelectionInterval(int index0, int index1) {
    super.setSelectionInterval(index0, index1);
    final int firstIndex = index0 > index1 ? index1 : index0;
    final int secondIndex = index0 > index1 ? index0 : index1;
    final List<CharacterI> selectedCharacters = this.characterListManager.getCharacterList().getList().subList(firstIndex, secondIndex + 1);
    this.selectionManager.selectCharacters(this, new ArrayList<CharacterI>(selectedCharacters));
  }
  
  public void clearSelection() {
    // only clear the selection if there are no characters
    if (this.getCharacterCount() == 0) {
      super.clearSelection();
      return;
    }
    // otherwise select a character
    final int selectedIndex = this.getMinSelectionIndex();
    if (selectedIndex > -1) {
      final int maximumSelectedIndex = selectedIndex < this.getCharacterCount() ? selectedIndex : (this.getCharacterCount() - 1);
      this.setSelectionInterval(maximumSelectedIndex, maximumSelectedIndex);
    } else {
      this.setSelectionInterval(0, 0);
    }
  }
  
  public void charactersSelected(CharSelectionEvent e) {
    if (e.getSource() == this) {
      return;
    } else {
      this.setSelectionInterval(e.getChars());
    }
  }

  private int getCharacterCount() {
    return this.characterListManager.getCharacterList().size();
  }
  
  private void setSelectionInterval(List<CharacterI> characters) {
    if ((characters == null) || (characters.isEmpty()) || (!this.characterListManager.getCharacterList().getList().containsAll(characters))) {
      this.clearSelection();
      return;
    }
    int minIndex = Integer.MAX_VALUE;
    int maxIndex = Integer.MIN_VALUE;
    final List<CharacterI> allCharacters = this.characterListManager.getCharacterList().getList();
    for (CharacterI character : characters) {
      final int characterIndex = allCharacters.indexOf(character);
      if (characterIndex < minIndex) {
        minIndex = characterIndex;
      }
      if (characterIndex > maxIndex) {
        maxIndex = characterIndex;
      }
    }
    this.setSelectionInterval(minIndex, maxIndex);
  }
  
}
