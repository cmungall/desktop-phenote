package org.phenoscape.view;

import javax.swing.table.DefaultTableCellRenderer;

import org.obo.datamodel.OBOClass;

public class TermRenderer extends DefaultTableCellRenderer {

  @Override
  protected void setValue(Object value) {
    if (value != null) {
      final OBOClass term = (OBOClass)value;
      this.setText(term.getName());
      this.setToolTipText(term.getID());
    } else {
      this.setText("");
      this.setToolTipText(null);
    }
  }
  
}
