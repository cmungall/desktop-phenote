package phenote.gui;

import java.awt.Frame;
import javax.swing.JDialog;

import phenote.datamodel.CharField;
import phenote.datamodel.CharacterI;
import phenote.config.Config;
import phenote.gui.field.FieldPanel;

/** a gui for making comparisons between 2 statements/annotations */

class Comparison {
  
  private JDialog dialog;
  private CharacterI char1;
  private CharacterI char2;

  Comparison(Frame owner, CharacterI c1, CharacterI c2) {
    init(owner,c1,c2);
  }

  private void init(Frame owner, CharacterI c1, CharacterI c2) {
    char1 = c1;
    char2 = c2;
    dialog = new JDialog(owner,"Statement Comparison");

    FieldPanel fieldPanel = FieldPanel.makeBasicPanel();
    dialog.add(fieldPanel);
    
    fieldPanel.addLabelInNewRow(charString(c1));

    // Relationship - dislpay rel if comp already made

    fieldPanel.addLabelInNewRow(charString(c2));

    // Buttons OK & Cancel

    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
  }

  // util fn?
  private String charString(CharacterI c) {
    StringBuffer sb = new StringBuffer();
    for (CharField cf : c.getAllCharFields()) {
      if (!Config.inst().isVisible(cf)) continue;
      String val = c.getValueString(cf);
      if (val == null || val.trim().equals("")) continue;
      sb.append(val).append(" ");
    }
    return sb.toString().trim();
  }

  // OK

  // Cancel

}
