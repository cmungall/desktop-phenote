package phenote.gui;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

public class TableSortingAdapter extends MouseAdapter {
  private JTable table;
  private SortableTableModel model;

  public TableSortingAdapter(JTable theTable, SortableTableModel theModel) {
    super();
    this.table = theTable;
    this.model = theModel;
  }
  
  public void mouseClicked(MouseEvent e) {
    if (e.getClickCount() != 1) return;
    final Point p = e.getPoint();
    final int viewColumnIndex = this.table.getTableHeader().columnAtPoint(p);
    final int modelColumnIndex = this.table.convertColumnIndexToModel(viewColumnIndex);
    if (modelColumnIndex == -1) return;
    this.model.sortOnColumn(modelColumnIndex);
  }
  
}
