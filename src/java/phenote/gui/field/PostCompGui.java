package phenote.gui.field;

import java.util.ArrayList;
import java.util.List;
import java.awt.Dimension;
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
import org.geneontology.oboedit.datamodel.OBORestriction;
import org.geneontology.oboedit.datamodel.impl.OBOClassImpl;
import org.geneontology.oboedit.datamodel.impl.OBOPropertyImpl;
import org.geneontology.oboedit.datamodel.impl.OBORestrictionImpl;

import phenote.datamodel.CharacterI;
import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.OboUtil;
import phenote.datamodel.Ontology;
//import phenote.datamodel.SearchParamsI;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.EditManager;
//import phenote.edit.UpdateTransaction;
import phenote.edit.CompoundTransaction;
import phenote.main.Phenote;
import phenote.gui.selection.CharSelectionEvent;
import phenote.gui.selection.CharSelectionListener;
import phenote.gui.selection.SelectionManager;

import phenote.gui.SearchParams;
import phenote.gui.SearchParamsI;

/** A window for post composition and other wacky stuff that goes beyond the basic
    stuff in field (eg relational quality?) 
    may want to be able to give up frame to put inline as well as window? 
    as window may be configurable or scrapped */
class PostCompGui {

  private CharField charField;
  private JDialog dialog;
  // eventually with embedding this will be a TermGui
  private CharFieldGui genusField;
  //private RelationshipFieldGui relField; ??
  //private CharFieldGui relField;
  //private CharFieldGui diffField;
  private List<RelDiffGui> relDiffGuis = new ArrayList<RelDiffGui>(3);
  private SearchParamsI searchParams;
  private FieldPanel compFieldPanel;

  PostCompGui(CharField charField,SearchParamsI searchParams) {
    this.charField = charField;
    this.searchParams = searchParams;
    init();
  }

  private class RelDiffGui {
    private CharFieldGui relField;
    // with embedded/recurse this will be a TermGui...
    private CharFieldGui diffField;
    private RelDiffGui() {
      CharField relChar = new CharField(CharFieldEnum.RELATIONSHIP);
      Ontology o = charField.getPostCompRelOntol();
      relChar.addOntology(o);
      relField = CharFieldGui.makeRelationList(relChar,searchParams);//"Relationship"?
      compFieldPanel.addCharFieldGuiToPanel(relField);
      diffField = CharFieldGui.makePostCompTermList(charField,searchParams,"Differentia");
      compFieldPanel.addCharFieldGuiToPanel(diffField);
    }
    private void setRelDiffModel(RelDiffModel rd) {
//       try { rd.relField.setRel(getRel(currentTerm)); } catch (Exception e){}
      relField.setRel(rd.rel);
      diffField.setOboClass(rd.diff);
      diffField.setOntologyChooserFromTerm(rd.diff);
    }
  }

  private class RelDiffModel {
    private OBOProperty rel;
    private OBOClass diff;
    private RelDiffModel(OBORestriction link) {
      rel = link.getType();
      diff = (OBOClass)link.getParent();
    }
  }


  private void init() {
    // dialog wont be focusable if owner is not showing or something like that
    Frame owner = Phenote.getPhenote().getFrame();
    dialog = new JDialog(owner,charField.getName()+" Post Composition");
    compFieldPanel = new FieldPanel(false,false); // (searchParams)?
    compFieldPanel.setSearchParams(searchParams);
    
    // MAIN GENUS TERM
    genusField = CharFieldGui.makePostCompTermList(charField,searchParams,"Genus");
    compFieldPanel.addCharFieldGuiToPanel(genusField);

    // REL-DIFFS
    addRelDiffGui();

    // HARDWIRE 2ND REL&DIFF FOR NOW eventually put in refine button to add more diffs
    addRelDiffGui();

    // WHAT THE HELL HARDWIRE A 3RD
    addRelDiffGui();

//     // Relationship?? stripped down ontology? hmmmmmm...
//     CharField relChar = new CharField(CharFieldEnum.RELATIONSHIP);
//     // Ontology o = OntologyManager.getRelationshipOntology() ?? getRelCharField?
//     Ontology o = charField.getPostCompRelOntol();
//     relChar.addOntology(o);
//     //relField = new CharFieldGui(relChar,compFieldPanel,"Relationship",false,false);
//     relField = CharFieldGui.makeRelationList(relChar,searchParams); // "Relationship"?
//     compFieldPanel.addCharFieldGuiToPanel(relField);
//     //relField.enableTermInfoListening(false); // turn off term info for rels
//     // relField = new RelFieldGui(relChar,compTermPanel,"Relationship");
//     // relField = new RelationshipFieldGui(compFieldPanel);

//     // when recurse put in flag for comp button
//     diffField = CharFieldGui.makePostCompTermList(charField,searchParams,"Differentia");
//     compFieldPanel.addCharFieldGuiToPanel(diffField);
//     //new CharFieldGui(charField,compFieldPanel,"Differentia",false,false);

    


    setGuiFromSelectedModel();

    // override FieldPanel preferred size which will set window size
    compFieldPanel.setPreferredSize(null);//new Dimension(700,160));
    dialog.add(compFieldPanel);
    addButtons();
    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);

    EditManager.inst().addCharChangeListener(new CompCharChangeListener());
    SelectionManager.inst().addCharSelectionListener(new CompCharSelectListener());
  }

  private void addRelDiffGui() {
    relDiffGuis.add(new RelDiffGui());
  }

  private void setGuiFromSelectedModel() {
    OBOClass currentTerm = getModelTerm();
    if (currentTerm == null) return;
    
    //genusField.setText(getGenusString(currentTerm));
    genusField.setOboClass(getGenusTerm(currentTerm));
    // should this happen automatically from setOboClass or is that a burden/inefficiency
    genusField.setOntologyChooserFromTerm(getGenusTerm(currentTerm));
    //if (modelHasDiff(currentTerm)) // should query if temp or real post comp??
    
    try {
      List<RelDiffModel>diffs = getRelDiffs(currentTerm);
      
      for (int i=0; i<diffs.size(); i++) {
        // check that have enough guis for diffs
        if (i >= relDiffGuis.size()) addRelDiffGui();
        relDiffGuis.get(i).setRelDiffModel(diffs.get(i));
      }
    }
    catch (CompEx e) { log().debug("get rel diffs failed "+e); }


//     for (RelDiffGui rd : relDiffGuis) {
//       try { rd.relField.setRel(getRel(currentTerm)); } catch (Exception e){}
//       try {
//         rd.diffField.setOboClass(getDiffTerm(currentTerm));
//         rd.diffField.setOntologyChooserFromTerm(getDiffTerm(currentTerm));
//       } 
//       catch (Exception e) {} // throws if no diff term
//     }
    
  }

  /** If model has changed (main window fiddling - hmmm) set gui */
  private class CompCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      setGuiFromSelectedModel();
    }
  }

  private class CompCharSelectListener implements CharSelectionListener {
    public void charactersSelected(CharSelectionEvent e) {
      setGuiFromSelectedModel();
    }
  }

  private OBOClass getModelTerm() {
    // there should be convenience method for this
    // multi select get 1st??
    CharacterI c = SelectionManager.inst().getFirstSelectedCharacter();
    //return charField.getCharFieldEnum().getValue(c).getOboClass();
    return c.getValue(charField).getOboClass();
  }

  // util fn?
  private boolean isPostCompTerm(OBOClass term) {
    for (Object o : term.getParents()) {
      if ( ((OBORestriction)o).completes() )
        return true;
    }
    return false;
  }

  //private String getGenusStr(OBOClass term){return getGenusTerm(term).getName();}

  /** for non post comp returns term itself */
  private OBOClass getGenusTerm(OBOClass term) {
    if (isPostCompTerm(term)) {
      for (Object o : term.getParents()) {
        OBORestriction r = (OBORestriction)o;
        if (r.completes() && r.getType().equals(OBOProperty.IS_A))
          return (OBOClass)r.getParent(); // check downcast?
      }
      // error msg?
    }
    return term;
  }

  private class CompEx extends Exception {
    private CompEx() {}
    private CompEx(String m) { super(m); }
  }

  private List<RelDiffModel> getRelDiffs(OBOClass term) throws CompEx {
    if (!isPostCompTerm(term)) throw new CompEx();
    List<RelDiffModel>diffs = new ArrayList<RelDiffModel>(4);
    for (Object o : term.getParents()) {
      OBORestriction r = (OBORestriction)o;
      if (isLinkToDiff(r)) {
        diffs.add(new RelDiffModel(r));
      }
      //return r;
    }
    if (diffs.isEmpty()) throw new CompEx(); // none found
    return diffs;
  }

  private boolean isLinkToDiff(OBORestriction r) {
    return r.completes() && !r.getType().equals(OBOProperty.IS_A);
  }

  /** Throws exception if no diff term - for now only returning one diff term
      can there be more than one */
  private OBOClass getDiffTerm(OBOClass term) throws CompEx {
    OBORestriction link = getDiffLink(term); // throws Ex
    return (OBOClass)link.getParent(); // check downcast?
  }

  /** If term is post comp return obo property relationship for differentia 
      otherwise thorws exception */
  private OBOProperty getRel(OBOClass term) throws CompEx {
    return getDiffLink(term).getType(); // throws ex
  }

  /** return the oboRestriction link between term and its differentia 
      exception if there is none. 
      This assumes that there is only one diff - no longer true! 
      should make specific ex*/
  private OBORestriction getDiffLink(OBOClass term) throws CompEx {
    if (!isPostCompTerm(term)) throw new CompEx();
    for (Object o : term.getParents()) {
      OBORestriction r = (OBORestriction)o;
      if (r.completes() && r.getType() != OBOProperty.IS_A)
        return r;
    }
    throw new CompEx(); // none found
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
      } catch (CompEx ex) {
        String m = "Post composition not fully filled in ";
        if (ex.getMessage()!=null) m+=ex.getMessage();
        log().debug(m); // ??
        JOptionPane.showMessageDialog(dialog,m,"error",JOptionPane.ERROR_MESSAGE);
        return; // dont dispose
      }
      dialog.dispose(); // keep up if failure?
    }
  }

  private OBOClass makePostCompTerm() throws CompEx {
    try {
      // check that we have a valid genus & differentia
      OBOClass genusTerm;
      try { genusTerm = genusField.getCurrentOboClass(); }
      catch (CharFieldGui.CharFieldGuiEx e) { throw new CompEx("Genus is unspecified"); }
      OboUtil oboUtil = OboUtil.initPostCompTerm(genusTerm);
      for (RelDiffGui rd : relDiffGuis) {
        // check if filled in both rel & diff
        try {
          OBOProperty rel = rd.relField.getCurrentRelation(); // throws ex
          OBOClass diffTerm = rd.diffField.getCurrentOboClass(); // throws Ex
          oboUtil.addRelDiff(rel,diffTerm);
        }
        catch (CharFieldGui.CharFieldGuiEx e) {} // try next diff
      }
      //return OboUtil.makePostCompTerm(genusTerm,rel,diffTerm);
      //if we didnt get a complete comp - genus rel diff - then throw ex
      if (!oboUtil.hasRelAndDiff()) throw new CompEx("Missing relation or diff");
      return oboUtil.getPostCompTerm();
    }
    catch (Exception e) { throw new CompEx(e.getMessage()); }
  }

  private void commitTerm(OBOClass postComp) {
    List<CharacterI> chrs = SelectionManager.inst().getSelectedChars();
    //CharFieldEnum cfe = charField.getCharFieldEnum();
    //CompoundTransaction ct = new CompoundTransaction(chrs,cfe,postComp);
    CompoundTransaction ct = CompoundTransaction.makeUpdate(chrs,charField,postComp);
    EditManager.inst().updateModel(this,ct);
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

//     String nm = pcString(genusTerm.getName(),diffTerm.getName());
//     String id = pcString(genusTerm.getID(),diffTerm.getID());
//     OBOClass postComp = new OBOClassImpl(nm,id);
//     OBOProperty ISA = OBOProperty.IS_A;
//     OBORestrictionImpl gRel = new OBORestrictionImpl(postComp,ISA,genusTerm);
//     gRel.setCompletes(true); // post comp flag
//     postComp.addParent(gRel);
//     OBORestrictionImpl dRel = new OBORestrictionImpl(postComp,partOf,diffTerm);
//     dRel.setCompletes(true); // post comp
//     postComp.addParent(dRel);
//     return postComp;
