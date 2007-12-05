package phenote.gui;

import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import org.bbop.framework.AbstractGUIComponent;

import phenote.datamodel.CharacterI;

/**
 * This is a beginning for an implementation of the character table as a 
 * GUIComponent.  The CharacterTableController should really be merged into this.
 * At the moment this class provides a place to implement action methods (e.g. Copy).
 */
public class CharacterTable extends AbstractGUIComponent {
  
  private static final long serialVersionUID = -3970995454868538116L;
  private CharacterTableController tableController;

  public CharacterTable(String group, String id) {
    super(id);
    this.tableController = new CharacterTableController(group);
    this.setLayout(new GridLayout());
    this.add(this.tableController.getCharacterTablePanel());
  }
  
  public void addNewCharacter() {
    this.tableController.addNewCharacter();
  }
  
  public void deleteSelectedCharacters() {
    this.tableController.deleteSelectedCharacters();
  }
  
  public void duplicateSelectedCharacters() {
    this.tableController.duplicateSelectedCharacters();
  }
  
  public void cut() {
    this.copy();
    this.tableController.deleteSelectedCharacters();
  }
  
  public void copy() {
    final StringBuffer buffer = new StringBuffer();
    for (CharacterI character :this.tableController.getSelectionModel().getSelected()) {
      buffer.append(character);
      buffer.append(System.getProperty("line.separator"));
    }
    if (buffer.length() > 0) {
      final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      final StringSelection stringSelection = new StringSelection(buffer.toString());
      clipboard.setContents(stringSelection, null);
    }
  }

}
