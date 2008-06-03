package phenote.gui;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import phenote.config.Config;
import phenote.config.FieldConfig;
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
    //return Config.inst().getEnbldFieldsNum(this.representedGroup);
    return getViewToModelColumns().size();
  }

  public String getColumnName(int viewColumn) {
    int modelColumn = viewColumnToModelColumn(viewColumn);
    return Config.inst().getFieldLabel(modelColumn, this.representedGroup);
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
  
  private CharField getCharFieldForColumn(int viewCol) {
    try {
      int modelCol = viewColumnToModelColumn(viewCol);
      return CharFieldManager.inst().getCharField(modelCol, this.representedGroup);
    } 
    catch (OntologyException e) {
      log().error("Column " + viewCol + " not configured properly in " + "character table, can't retrieve value ", e);
      return null;
    }
  }

  /** an array mapping view to model columns, 
      where view columns are the index of the array, and model column
      is the int value of the array for that index. If this is unintuitive i can
      refactor as hash or something */
  //private int[] viewToModelColumns; // = new int[];
  private List<Integer> viewToModelColumns;
  /** view columns dont include hidden fields, model columns do. This takes a view column
      number and returns the model column number for it. So if column 2 is hidden
      (and 0 & 1 are not hidden), then view column 2 is model column 3.
      should this go in Config? */
  private int viewColumnToModelColumn(int viewColumn) {
    // returns modelColumn for index of viewColumn
    return getViewToModelColumns().get(viewColumn);
  }

  /** Mapping of view columns to model column number via a List structure
      would a hash be better? */
  private List<Integer> getViewToModelColumns() {
    if (viewToModelColumns==null) {
      int modelSize = Config.inst().getEnbldFieldsNum(this.representedGroup);
      viewToModelColumns = new ArrayList<Integer>();//new int[modelSize];
      // cache?? or just do on fly?
      //List<FieldConfig> modelFC = Config.inst().getFieldCfgsInGroup(representedGroup);
      
      int viewCol = 0;
      for (int modelCol=0; modelCol<modelSize; modelCol++) {
        FieldConfig fc = Config.inst().getEnbldFieldCfg(modelCol,representedGroup);
        if (fc.isVisible())
          viewToModelColumns.add(modelCol);
          //viewToModelColumns[viewCol++] = modelCol;
      }
    }
    return viewToModelColumns;
  }
  
  private static Logger log() {
    return Logger.getLogger(CharacterTableFormat.class);
  }

}
