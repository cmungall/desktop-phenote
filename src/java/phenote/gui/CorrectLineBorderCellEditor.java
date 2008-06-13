package phenote.gui;

import java.awt.Color;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

/**
 * This cell editor draws its borders the same way the built-in cell editor does.
 * @author Jim Balhoff
 */
public class CorrectLineBorderCellEditor extends DefaultCellEditor {

  public CorrectLineBorderCellEditor(JTextField textField) {
    super(textField);
    textField.setBorder(new LineBorder(Color.black));
  }

  public CorrectLineBorderCellEditor(JCheckBox checkBox) {
    super(checkBox);
    checkBox.setBorder(new LineBorder(Color.black));
  }

  public CorrectLineBorderCellEditor(JComboBox comboBox) {
    super(comboBox);
    comboBox.setBorder(new LineBorder(Color.black));
  }

}
