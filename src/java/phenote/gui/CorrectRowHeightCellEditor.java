package phenote.gui;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * This cell editor makes sure the table row it's embedded in is tall enough.
 * @author Jim Balhoff
 */
public class CorrectRowHeightCellEditor extends DefaultCellEditor {

  public CorrectRowHeightCellEditor(JTextField textField) {
    super(textField);
  }

  public CorrectRowHeightCellEditor(JCheckBox checkBox) {
    super(checkBox);
  }

  public CorrectRowHeightCellEditor(JComboBox comboBox) {
    super(comboBox);
  }
  
  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    final Component component = super.getTableCellEditorComponent(table, value, isSelected, row, column);
    if (table != null) {
      // JTable makes text editor too small, so we have to force it
      table.setRowHeight(row, (int)(Math.ceil(component.getPreferredSize().getHeight())));
    }
    return component;
  }

}
