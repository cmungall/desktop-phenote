package phenote.gui;

import java.util.Comparator;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.OntologyException;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;

import com.eekboom.utils.Strings;

public class CharacterTableFormat implements AdvancedTableFormat<CharacterI> , WritableTableFormat<CharacterI> {
  
  private String representedGroup = "default";
  
  public CharacterTableFormat(String groupName) {
    if (representedGroup != null) this.representedGroup = groupName;
  }

  public int getColumnCount() {
    return Config.inst().getEnbldFieldsNum(this.representedGroup);
  }

  public String getColumnName(int column) {
    return Config.inst().getFieldLabel(column, this.representedGroup);
  }

  public Object getColumnValue(CharacterI character, int column) {
      final CharField cf = this.getCharFieldForColumn(column);
      return character.getValueString(cf);
  }
  
  public Class<?> getColumnClass(int column) {
    return String.class;
  }

  public Comparator<?> getColumnComparator(int column) {
    return Strings.getNaturalComparator();
  }
  
  public boolean isEditable(CharacterI character, int column) {
    final CharField cf = this.getCharFieldForColumn(column);
    return cf.isFreeText() || cf.isTerm();
  }

  public CharacterI setColumnValue(CharacterI character, Object editedValue, int column) {
    return character;
  }
  
  private CharField getCharFieldForColumn(int column) {
    try {
      return CharFieldManager.inst().getCharField(column, this.representedGroup);
    } 
    catch (OntologyException e) {
      log().error("Column " + column + " not configured properly in " + "character table, can't retrieve value ", e);
      return null;
    }
  }
  
  private static Logger log() {
    return Logger.getLogger(CharacterTableFormat.class);
  }

}
