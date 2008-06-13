package org.phenoscape.view;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.JTextField;

import org.obo.datamodel.OBOClass;
import org.phenoscape.model.TermSet;

import phenote.gui.CorrectLineBorderCellEditor;

// this class is temporary, unfinished, and just for playing around
public class TermEditor extends CorrectLineBorderCellEditor implements ActionListener {
  
  private OBOClass currentTerm;
  private final JTextField textField;
  private final TermSet termSet;

  public TermEditor(JTextField textField, TermSet terms) {
    super(textField);
    this.textField = textField;
    this.textField.addActionListener(this);
    this.termSet = terms;
  }

  @Override
  public Object getCellEditorValue() {
    return this.currentTerm;
  }

  @Override
  public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
    this.currentTerm = (OBOClass)value;
    final String editValue = this.currentTerm == null ? "" : this.currentTerm.getName();
    return super.getTableCellEditorComponent(table, editValue, isSelected, row, column);
  }

  public void actionPerformed(ActionEvent e) {
    final String candidateText = this.textField.getText();
    final Collection<OBOClass> terms = this.termSet.getTerms();
    for (OBOClass term : terms) {
      if (term.getName().equals(candidateText)) {
        this.currentTerm = term;
        return;
      }
    }
    this.currentTerm = null;
  }
  
}
