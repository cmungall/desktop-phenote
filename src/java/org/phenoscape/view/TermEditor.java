package org.phenoscape.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.obo.datamodel.OBOClass;

// this class is temporary, unfinished, and just for playing around
public class TermEditor extends DefaultCellEditor implements ActionListener {
  
  private OBOClass currentTerm;
  private final JTextField textField;

  public TermEditor(JTextField textField) {
    super(textField);
    this.textField = textField;
    this.textField.addActionListener(this);
  }

  @Override
  public Object getCellEditorValue() {
    return this.currentTerm;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    this.currentTerm = (OBOClass)value;
    return super.getTableCellEditorComponent(table, "hello", isSelected, row, column);
  }

  public void actionPerformed(ActionEvent e) {
    System.out.println(this.textField.getText());
  }
  
}
