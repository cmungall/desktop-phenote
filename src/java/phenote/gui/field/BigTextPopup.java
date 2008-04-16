package phenote.gui.field;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Frame;
import java.util.List;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import phenote.edit.EditManager;

/** A window popup for editing big text fields. it saves real estate in field panel to
    have this as a separate window. for users to edit comments, descriptions... where
    the tiny field is too tiny */
class BigTextPopup {

  private JDialog dialog;
  private JLabel topLabel;
  private JTextArea textArea;
  private CharField charField;
  private List<CharacterI> charsToEdit;

  BigTextPopup(Frame owner,CharField cf,List<CharacterI> chars) {
    
    charField = cf;
    charsToEdit = chars;

    dialog = new JDialog(owner,"Enter text for "+charField.getName(),true); // true modal
    
    topLabel = new JLabel(charField.getName()+":");
    dialog.add(topLabel,BorderLayout.NORTH);

    textArea = new JTextArea(15,60);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    populateTextFromModel();
    JScrollPane jsp = new JScrollPane(textArea);
    dialog.add(jsp,BorderLayout.CENTER);

    JButton ok = new JButton("OK");
    ok.addActionListener(new OkListener());
    
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new CancelListener());
    
    JPanel buttons = new JPanel();
    buttons.add(ok);
    buttons.add(cancel);
    dialog.add(buttons,BorderLayout.SOUTH);

    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);

  }

  private String field() { return charField.getName(); }

  private void populateTextFromModel() {
    if (charsToEdit.isEmpty()) return;
    String firstVal = charsToEdit.get(0).getValueString(charField);
    for (CharacterI c : charsToEdit) {
      if (!c.getValueString(charField).equals(firstVal)) {
        textArea.setText("");
        String s = "<html>WARNING!<br>Multiple rows selected with different values for "
          +field()+"<br>Are you sure you want to edit?<br>Hit cancel to exit.<br>";
        topLabel.setText(s+topLabel.getText());
        return;
      }
    }
    textArea.setText(firstVal);
  }

  /** user hit ok -> commit text to datamodel and close */
  private class OkListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      String val = textArea.getText();
      EditManager.inst().updateModel(charsToEdit,charField,val,this);
      dialog.dispose();
    }
  }

  private class CancelListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      dialog.dispose();
    }
  }

}
