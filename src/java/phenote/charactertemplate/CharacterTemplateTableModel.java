package phenote.charactertemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.dataadapter.CharListChangeEvent;
import phenote.dataadapter.CharListChangeListener;
import phenote.dataadapter.CharacterListManager;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharacterI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;

@SuppressWarnings("serial")
public class CharacterTemplateTableModel extends AbstractTableModel implements CharChangeListener, CharListChangeListener, TemplateChoiceListener {
  
  private String representedGroup;
  private CharacterListManager characterListManager;
  private EditManager editManager;
  private Set<CharacterI> markedCharacters;
  private static final String SELECTED_COLUMN_NAME = "Use";

  public CharacterTemplateTableModel(String groupName, CharacterListManager clManager, EditManager eManager) {
    super();
    this.representedGroup = groupName;
    this.characterListManager = clManager;
    this.characterListManager.addCharListChangeListener(this);
    this.editManager = eManager;
    this.editManager.addCharChangeListener(this);
    this.markedCharacters = new HashSet<CharacterI>();
  }
  
  public List<CharacterI> getMarkedCharacters() {
    // make a list to make sure the characters are in the same order as they are in the main character list
    List<CharacterI> characters = new ArrayList<CharacterI>();
    for (CharacterI character : this.getAllCharacters()) {
      if (this.isCharacterMarked(character)){
        characters.add(character);
      }
    }
    return characters;
  }
  
  public void setMarkedCharacters(Collection<CharacterI> charactersToMark) {
    for (CharacterI character : this.getAllCharacters()) {
      this.setCharacterIsMarked(character, charactersToMark.contains(character));
    }
  }
  
  public boolean isCharacterMarked(CharacterI character) {
    return this.markedCharacters.contains(character);
  }
  
  public void setCharacterIsMarked(CharacterI character, boolean selected) {
    if (selected) {
      this.markedCharacters.add(character);
    } else {
      this.markedCharacters.remove(character);
    }
    final int indexOfCharacter = this.getAllCharacters().indexOf(character);
    this.fireTableRowsUpdated(indexOfCharacter, indexOfCharacter);
  }
  
  public void invertCharacterMarks() {
    for (CharacterI character : this.getAllCharacters()) {
      this.setCharacterIsMarked(character, !this.isCharacterMarked(character));
    }
  }
  
  public void setCharactersInRowsAreMarked(int[] rows, boolean marked) {
    for (int row : rows) {
      this.setCharacterIsMarked(this.getCharacterAtRow(row), marked);
    }
  }

  public int getColumnCount() {
    return Config.inst().getFieldsInGroup(this.representedGroup).size() + 1;
  }

  public int getRowCount() {
    return this.getAllCharacters().size();
  }

  public Object getValueAt(int row, int column) {
    final CharacterI character = this.getCharacterAtRow(row);
    final String fieldName = this.getColumnName(column);
    if (fieldName == CharacterTemplateTableModel.SELECTED_COLUMN_NAME) {
      return this.isCharacterMarked(character);
    }
    try {
      return character.getValueString(fieldName);
    } catch (CharFieldException e) {
      this.getLogger().error("Failed to get value for table column", e);
      return "";
    }
  }
  
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    if (this.getColumnName(columnIndex).equals(CharacterTemplateTableModel.SELECTED_COLUMN_NAME)) {
      this.setCharacterIsMarked(this.getCharacterAtRow(rowIndex), (Boolean)aValue);
    }
  }

  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == 0) {
      return Boolean.class;
    } else {
      return super.getColumnClass(columnIndex);
    }
  }

  public String getColumnName(int column) {
    if (column == 0) {
      return CharacterTemplateTableModel.SELECTED_COLUMN_NAME;
    } else {
      return Config.inst().getFieldsInGroup(this.representedGroup).get(column - 1);
    }
  }
  
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 0;
  }
  
  public void charChanged(CharChangeEvent e) {
    if (e.isUpdate()) {
      this.fireTableRowsUpdated(0, this.getRowCount() - 1);
    } else if (e.isAdd()) {
      this.fireTableRowsInserted(this.getRowCount() - 1, this.getRowCount() - 1);
    } else { // must be a deletion
      this.fireTableDataChanged();
    }
  }
  
  public void newCharList(CharListChangeEvent e) {
    this.fireTableDataChanged();
  }

  public void templateChoiceChanged(TemplateChooser source) {
    this.setMarkedCharacters(source.getChosenTemplates(Collections.unmodifiableList(this.getAllCharacters())));
  }
  
  private CharacterI getCharacterAtRow(int row) {
    return this.characterListManager.getCharacterList().get(row);
  }
  
  private List<CharacterI> getAllCharacters() {
    return this.characterListManager.getCharacterList().getList();
  }

  private Logger getLogger() {
    return Logger.getLogger(this.getClass());
  }

}
