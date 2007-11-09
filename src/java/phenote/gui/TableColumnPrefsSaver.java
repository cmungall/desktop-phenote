package phenote.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import org.apache.log4j.Logger;

/**
 * Persists user changes to column widths and order for a table using java.util.prefs.  Column
 * titles should be unique within the table.  If the column content (count or titles) is changed
 * between uses of a table, columns will be reset to the default order until the user moves them
 * again.
 */
public class TableColumnPrefsSaver implements PropertyChangeListener, TableColumnModelListener {
  
  private int defaultColumnWidth;
  private JTable table;
  private String autoSaveName;
  
  /**
   * Constructs a TableColumnPrefsSaver for the given table using a default column width of 150. 
   * Column widths and order are persisted to the preferences datastore.  The autoSaveName should
   *  be sufficiently unique to avoid conflicts with saved states for other tables.
   */
  public TableColumnPrefsSaver(JTable aTable, String autoSaveName) {
    this(aTable, autoSaveName, 150);
  }
  
  /**
   * Constructs a TableColumnPrefsSaver for the given table.  Column widths and order are persisted
   * to the preferences datastore.  The autoSaveName should be sufficiently unique to avoid conflicts
   * with saved states for other tables.  If no column width values are already saved for this table, the 
   * defaultColumnWidth will be used.
   */
  public TableColumnPrefsSaver(JTable aTable, String autoSaveName, int defaultColumnWidth) {
    this.table = aTable;
    this.autoSaveName = autoSaveName;
    this.defaultColumnWidth = defaultColumnWidth;
    this.table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    this.sizeColumns();
    for (TableColumn column : this.getColumns()) {
      column.addPropertyChangeListener(this);
    }
    this.orderColumns();
    this.table.getColumnModel().addColumnModelListener(this);
  }

  public void propertyChange(PropertyChangeEvent e) {
    if (e.getPropertyName().equals("width")) {
      this.saveColumnWidth((TableColumn)(e.getSource()));
    }
  }

  public void columnAdded(TableColumnModelEvent e) {
    this.saveColumnOrdering();
  }

  public void columnMarginChanged(ChangeEvent e) {}

  public void columnMoved(TableColumnModelEvent e) {
    this.saveColumnOrdering();
  }

  public void columnRemoved(TableColumnModelEvent e) {
    this.saveColumnOrdering();
  }

  public void columnSelectionChanged(ListSelectionEvent e) {}
  
  private void sizeColumns() {
    for (TableColumn column : this.getColumns()) {
      final int width = this.getWidthPrefs().getInt(this.getColumnName(column), this.defaultColumnWidth);
      column.setPreferredWidth(width);
    }
  }
  
  private void saveColumnWidth(TableColumn column) {
    this.getWidthPrefs().putInt(column.getHeaderValue().toString(), column.getWidth());
  }
  
  private void orderColumns() {
    final List<String> prefNames;
    try {
      prefNames = Arrays.asList(this.getOrderPrefs().keys());
    } catch (BackingStoreException e) {
      log().error("Failed to read table column order from prefs", e);
      return;
    }
    final List<String> columnNames = this.getAllColumnNames();
    if (prefNames.containsAll(columnNames) && columnNames.containsAll(prefNames)) {
      for (TableColumn column : this.getColumns()) {
        final int newIndex = this.getOrderPrefs().getInt(this.getColumnName(column), 0);
        final int currentIndex = this.getIndexOfColumn(column);
        final int columnCount = this.table.getColumnCount();
        if ((newIndex < columnCount) && (currentIndex < columnCount)) {
          this.table.getColumnModel().moveColumn(currentIndex, newIndex); 
        }
      }
    }
    this.saveColumnOrdering();
  }
  
  private void saveColumnOrdering() {
    try {
      this.getOrderPrefs().clear();
    } catch (BackingStoreException e) {
      log().error("Unable to store table column ordering", e);
      return;
    }
    for (int i = 0; i < this.table.getColumnModel().getColumnCount(); i++) {
      this.getOrderPrefs().putInt(getColName(i),i);
                                  //this.table.getColumnName(i), i);
    }
  }

  /** if name too long then barfs - should use datatags! much shorter */
  private String getColName(int i) {
    String n = table.getColumnName(i);
    int max = 80;
    if (n.length() > max) n = n.substring(0,max);
    return n;
  }
  
  private String getColumnName(TableColumn column) {
    return column.getHeaderValue().toString();
  }
  
  private List<String> getAllColumnNames() {
    List<String> names = new ArrayList<String>();
    for (TableColumn column : this.getColumns()) {
      names.add(this.getColumnName(column));
    }
    return names;
  }
  
  private List<TableColumn> getColumns() {
    return Collections.list(this.table.getColumnModel().getColumns());
  }
  
  private int getIndexOfColumn(TableColumn column) {
    return this.getColumns().indexOf(column);
  }
  
  private Preferences getPrefsRoot() {
    return Preferences.userNodeForPackage(this.getClass()).node(this.autoSaveName);
  }
  
  private Preferences getWidthPrefs() {
    return this.getPrefsRoot().node("width");
  }
  
  private Preferences getOrderPrefs() {
    return this.getPrefsRoot().node("order");
  }
  
  private static Logger log() {
    return Logger.getLogger(TableColumnPrefsSaver.class);
  }

}
