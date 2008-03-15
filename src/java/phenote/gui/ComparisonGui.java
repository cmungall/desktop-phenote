package phenote.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import org.obo.datamodel.OBOProperty;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharFieldException;
import phenote.datamodel.CharFieldManager;
import phenote.datamodel.CharacterI;
import phenote.datamodel.CharacterEx;
import phenote.datamodel.Comparison;
import phenote.datamodel.Ontology;
import phenote.edit.EditManager;
import phenote.config.Config;
import phenote.gui.field.CharFieldGui;
import phenote.gui.field.CharFieldGuiEx;
import phenote.gui.field.FieldPanel;
import phenote.gui.field.ReadOnlyFieldGui;

/** a gui for making comparisons between 2 statements/annotations */

class ComparisonGui {
  
  private JDialog dialog;
//   private CharacterI char1;
//   private CharacterI char2;
  private CharFieldGui relFieldGui;
  /** eventually may do a list of comparisons? */
  private Comparison comparison;

  ComparisonGui(Frame owner, CharacterI c1, CharacterI c2) {
    try { init(owner,c1,c2); } 
    catch (CharFieldException x) {
      String m = "No relation ontology configured, cant do comparison";
      JOptionPane.showMessageDialog(null,m,"Error",JOptionPane.ERROR_MESSAGE);
    }
  }

  /** initialize gui with 2 characters (and frame owner) */
  private void init(Frame owner, CharacterI sub, CharacterI obj) 
    throws CharFieldException {
    comparison = new Comparison();
    comparison.setSubject(sub);
    comparison.setObject(obj);
    boolean modal = false;//true;
    dialog = new JDialog(owner,"Statement Comparison",modal);
    dialog.setAlwaysOnTop(true);

    FieldPanel fieldPanel = FieldPanel.makeBasicPanel();
    dialog.add(fieldPanel);
    
    // Statement 1
    //fieldPanel.addLabelForWholeRow(charString(comparison.getSubject()));
    ReadOnlyFieldGui subGui = new ReadOnlyFieldGui(new CharField("Subject",null,null));
    //r.setText(charString(comparison.getSubject()));
    subGui.setCharacter(comparison.getSubject()); // ???
    subGui.enableCharDropListening(true);
    fieldPanel.addCharFieldGuiToPanel(subGui);

    // Relationship - dislpay rel if comp already made
    addRelGui(fieldPanel); // throws CharFieldException if no rel ontology

    // Statement 2
    //fieldPanel.addLabelForWholeRow(charString(comparison.getObject()));
    ReadOnlyFieldGui objGui = new ReadOnlyFieldGui(new CharField("Object",null,null));
    //r.setText(charString(comparison.getSubject()));
    objGui.setCharacter(comparison.getObject()); // ???
    objGui.enableCharDropListening(true);
    fieldPanel.addCharFieldGuiToPanel(objGui);

    // Buttons OK & Cancel
    addButtons(fieldPanel);
    
    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);
    dialog.pack(); // ?
  }

  private void addRelGui(FieldPanel fp) throws CharFieldException {
    CharField relChar = new CharField(CharFieldEnum.RELATIONSHIP);
    // throws CharFieldEx if no rel ontol found
    Ontology o = CharFieldManager.inst().getComparisonRelationOntology();
    relChar.addOntology(o);
    relFieldGui = CharFieldGui.makeRelationList(relChar);//"Relationship"?
    fp.addCharFieldGuiToPanel(relFieldGui);
  }


  private void addButtons(FieldPanel fieldPanel) {
    List<JButton> buttons = new ArrayList<JButton>(2);
    JButton ok = new JButton("OK");
    ok.addActionListener(new OkListener());
    buttons.add(ok);
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new CancelListener());
    buttons.add(cancel);
    fieldPanel.addButtonRow(buttons);
  }

  // OK
  private class OkListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      try {
        commitComparison();
      } catch (CharFieldGuiEx x) {
        String m = "Comparison failed "+x.getMessage();
        log().debug(m);
        JOptionPane.showMessageDialog(dialog,m,"error",JOptionPane.ERROR_MESSAGE);
        return; // leave window up - hit cancel to get rid of
      }
      dialog.dispose(); // no ex - ok
    }
  }

  private void commitComparison() throws CharFieldGuiEx {
    OBOProperty rel = relFieldGui.getCurrentRelation(); // ex if not filled in
    comparison.setRelation(rel);
    try {
      //char1.addComparison(rel,char2);
      EditManager.inst().addComparison(this,comparison);
    } 
    catch (CharacterEx e) {
      String m = e.getMessage();
      JOptionPane.showMessageDialog(dialog,m,"error",JOptionPane.ERROR_MESSAGE);
      return;
    }
  }

  // CANCEL
  private class CancelListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      dialog.dispose();
    }
  }


  // util fn?
//   private String charString(CharacterI c) {
//     return ReadOnlyFieldGui.charString(c);
//   }
//     if (c == null) return "";
//     StringBuffer sb = new StringBuffer();
//     for (CharField cf : c.getAllCharFields()) {
//       if (!Config.inst().isVisible(cf)) continue;
//       String val = c.getValueString(cf);
//       if (val == null || val.trim().equals("")) continue;
//       sb.append(val).append(" ");
//     }
//     return sb.toString().trim();

  private Logger log() {
    return Logger.getLogger(getClass());
  }


}
