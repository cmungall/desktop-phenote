package phenote.gui;

import java.util.Comparator;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.OntologyException;
import ca.odell.glazedlists.gui.AdvancedTableFormat;

import com.eekboom.utils.Strings;

public class CharacterTableFormat implements AdvancedTableFormat<CharacterI> {
  
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
    try {
      CharField cf = CharFieldManager.inst().getCharField(column, this.representedGroup);
      return character.getValueString(cf);
    } 
    catch (OntologyException e) {
      log().error("Column " + column + " not configured properly in " + "character table, can't retrieve value ", e);
      return "";
    }
  }
  
  public Class<?> getColumnClass(int column) {
    return String.class;
  }

  public Comparator<?> getColumnComparator(int column) {
    return Strings.getNaturalComparator();
  }

  private static Logger log() {
    return Logger.getLogger(CharacterTableFormat.class);
  }

}
