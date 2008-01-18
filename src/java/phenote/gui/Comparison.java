package phenote.gui;

import java.awt.Frame;
import java.awt.GridBagConstraints;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.Ontology;
import phenote.config.Config;
import phenote.gui.field.CharFieldGui;
import phenote.gui.field.FieldPanel;

/** a gui for making comparisons between 2 statements/annotations */

class Comparison {
  
  private JDialog dialog;
  private CharacterI char1;
  private CharacterI char2;

  Comparison(Frame owner, CharacterI c1, CharacterI c2) {
    try { init(owner,c1,c2); } 
    catch (CharFieldException x) {
      String m = "No relation ontology configured, cant do comparison";
      JOptionPane.showMessageDialog(null,m,"Error",JOptionPane.ERROR_MESSAGE);
    }
  }

  private void init(Frame owner, CharacterI c1, CharacterI c2) 
    throws CharFieldException {
    char1 = c1;
    char2 = c2;
    dialog = new JDialog(owner,"Statement Comparison");

    FieldPanel fieldPanel = FieldPanel.makeBasicPanel();
    dialog.add(fieldPanel);
    
    fieldPanel.addLabelForWholeRow(charString(c1));

    // Relationship - dislpay rel if comp already made
    addRelGui(fieldPanel); // throws CharFieldException if no rel ontology

    fieldPanel.addLabelForWholeRow(charString(c2));

    // Buttons OK & Cancel

    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
  }

  private void addRelGui(FieldPanel fp) throws CharFieldException {
    CharField relChar = new CharField(CharFieldEnum.RELATIONSHIP);
    // throws CharFieldEx if no rel ontol found
    Ontology o = CharFieldManager.inst().getComparisonRelationOntology();
    relChar.addOntology(o);
    CharFieldGui relField = CharFieldGui.makeRelationList(relChar);//"Relationship"?
    fp.addCharFieldGuiToPanel(relField);
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
