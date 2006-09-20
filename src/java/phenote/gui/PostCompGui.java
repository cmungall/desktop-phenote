package phenote.gui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import org.geneontology.oboedit.datamodel.OBOClass;
import org.geneontology.oboedit.datamodel.OBOProperty;
import org.geneontology.oboedit.datamodel.impl.OBOClassImpl;
import org.geneontology.oboedit.datamodel.impl.OBOPropertyImpl;
import org.geneontology.oboedit.datamodel.impl.OBORestrictionImpl;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.SearchParamsI;
import phenote.edit.EditManager;
import phenote.edit.UpdateTransaction;
import phenote.main.Phenote;
import phenote.gui.selection.SelectionManager;

/** A window for post composition and other wacky stuff that goes beyond the basic
    stuff in field (eg relational quality?) 
    may want to be able to give up frame to put inline as well as window? 
    as window may be configurable or scrapped */
class PostCompGui {

  private CharField charField;
  private JDialog dialog;
  private CharFieldGui genusField;
  private CharFieldGui differentiaField;

  PostCompGui(CharField charField,SearchParamsI searchParams) {
    this.charField = charField;
    init(searchParams);
  }

  private void init(SearchParamsI searchParams) {
    // dialog wont be focusable if owner is not showing or something like that
    Frame owner = Phenote.getPhenote().getFrame();
    dialog = new JDialog(owner,charField.getName()+" Post Composition");
    TermPanel compTermPanel = new TermPanel(false); // (searchParams)?
    compTermPanel.setSearchParams(searchParams);
    
    // false - dont edit model
    genusField = new CharFieldGui(charField,compTermPanel,"Genus",false);

    // Relationship?? stripped down ontology?

    differentiaField = new CharFieldGui(charField,compTermPanel,"Differentia",false);

    dialog.add(compTermPanel);
    addButtons();
    dialog.pack();
    dialog.setVisible(true);
  }

  private void addButtons() {
    JPanel buttonPanel = new JPanel();
    JButton ok = new JButton(new OkAction());
    buttonPanel.add(ok);
    JButton cancel = new JButton(new CancelAction());
    buttonPanel.add(cancel);
    dialog.add(buttonPanel,BorderLayout.SOUTH);
  }

  private class CancelAction extends AbstractAction {
    private CancelAction() { super("Cancel"); }
    public void actionPerformed(ActionEvent e) {
      //dialog.close();
      dialog.dispose();
    }
  }
  private class OkAction extends AbstractAction {
    private OkAction() { super("OK"); }
    public void actionPerformed(ActionEvent e) {
      try {
        OBOClass pc = makePostCompTerm();
        commitTerm(pc);
      } catch (Exception ex) {
        String m = "Post composition failed "+ex.getMessage();
        log().error(m);
        JOptionPane.showMessageDialog(dialog,m,"error",JOptionPane.ERROR_MESSAGE);
        return; // dont dispose
      }
      dialog.dispose(); // keep up if failure?
    }
  }

  private OBOClass makePostCompTerm() throws Exception {
    // check that we have a valid genus & differentia
    OBOClass genusTerm = genusField.getSelectedOboClass();
    OBOClass diffTerm = differentiaField.getSelectedOboClass();
    String nm = pcString(genusTerm.getName(),diffTerm.getName());
    String id = pcString(genusTerm.getID(),diffTerm.getID());
    OBOClass postComp = new OBOClassImpl(nm,id);
    OBOProperty ISA = OBOProperty.IS_A;
    OBORestrictionImpl gRel = new OBORestrictionImpl(postComp,ISA,genusTerm);
    postComp.addParent(gRel);
    // eventually get from obo relationship?
    OBOProperty partOf = new OBOPropertyImpl("OBO_REL:part_of","part_of");
    OBORestrictionImpl dRel = new OBORestrictionImpl(postComp,partOf,diffTerm);
    postComp.addParent(dRel);
    return postComp;
  }

  private void commitTerm(OBOClass postComp) {
    CharacterI c = SelectionManager.inst().getSelectedCharacter();
    CharFieldEnum cfe = charField.getCharFieldEnum();
    //OBOClass previousOboClass = cfe.getValue(c).getOboClass();
    UpdateTransaction ut = new UpdateTransaction(c,cfe,postComp);
    EditManager.inst().updateModel(this,ut);
  }

  private String pcString(String g, String d) {
    // for now hard wire to part_of
    return g+"^part_of("+d+")";
  }

  private Logger log;
  private Logger log() {
    if (log == null) log = Logger.getLogger(getClass());
    return log;
  }

}
