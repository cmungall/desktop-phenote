package phenote.charactertemplate;

import java.util.Comparator;

import phenote.datamodel.CharacterI;
import phenote.gui.CharacterTableFormat;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.gui.WritableTableFormat;

public class CharacterTemplateTableFormat extends CharacterTableFormat implements WritableTableFormat<CharacterI> {
  
  private static final String SELECTED_COLUMN_NAME = "Use";
  private final CharacterTemplateTable tableController;
  
  public CharacterTemplateTableFormat(String groupName, CharacterTemplateTable tableController) {
    super(groupName);
    this.tableController = tableController;
  }
  
  public Class<?> getColumnClass(int columnIndex) {
    if (columnIndex == 0) {
      return Boolean.class;
    } else {
      return super.getColumnClass(columnIndex - 1);
    }
  }

  public Comparator<?> getColumnComparator(int columnIndex) {
    if (columnIndex == 0) {
      return GlazedLists.booleanComparator();
    } else {
      return super.getColumnComparator(columnIndex - 1);
    }
  }

  public int getColumnCount() {
    return super.getColumnCount() + 1;
  }

  public String getColumnName(int column) {
    if (column == 0) {
      return CharacterTemplateTableFormat.SELECTED_COLUMN_NAME;
    } else {
      return super.getColumnName(column - 1);
    }
  }

  public Object getColumnValue(CharacterI character, int column) {
    if (column == 0) {
      return this.tableController.isCharacterMarked(character);
    } else {
      return super.getColumnValue(character, (column - 1));
    }
  }
  
  public boolean isEditable(CharacterI character, int column) {
    if (column == 0) {
      return true;
    } else {
      return super.isEditable(character, (column - 1));
    }
  }

  public CharacterI setColumnValue(CharacterI character, Object editedValue, int column) {
    if (column == 0) {
      this.tableController.setCharacterIsMarked(character, (Boolean)editedValue);
      return character;
    } else {
      return super.setColumnValue(character, editedValue, column);
    }
  }

}
