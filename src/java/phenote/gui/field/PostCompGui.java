package phenote.gui.field;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.obo.datamodel.Link;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOProperty;
import org.obo.datamodel.OBORestriction;

import phenote.datamodel.CharField;
import phenote.datamodel.CharFieldEnum;
import phenote.datamodel.CharacterI;
import phenote.datamodel.OboUtil;
import phenote.datamodel.Ontology;
import phenote.edit.CharChangeEvent;
import phenote.edit.CharChangeListener;
import phenote.edit.CompoundTransaction;
import phenote.edit.EditManager;
import phenote.gui.selection.SelectionManager;
import ca.odell.glazedlists.swing.EventSelectionModel;

//import phenote.gui.SearchParams;
//import phenote.gui.SearchParamsI;

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
  //private SearchParamsI searchParams = SearchParams.inst();
  private FieldPanel compFieldPanel;
  private EditManager editManager;
  private SelectionManager selectionManager;
  private EventSelectionModel<CharacterI> selectionModel;
  private Frame owner;
  private CompCharChangeListener compCharChangeListener;
  private CompCharSelectListener compCharSelectListener;
  /** # of chars to type to get completion in genus & diff - do we need for relation? */
  private int minCompChars=0;
  
  
  PostCompGui(CharField charField, EditManager eManager, SelectionManager selManager, EventSelectionModel<CharacterI> selModel,
              Frame ownerFrame, int minCompChars) {
    this.charField = charField;
    this.editManager = eManager;
    this.selectionManager = selManager;
    this.selectionModel = selModel;
    this.owner = ownerFrame;
    this.minCompChars = minCompChars;
    init();
  }



  private void init() {
    // dialog wont be focusable if owner is not showing or something like that
   
    dialog = new JDialog(this.owner, charField.getName() + " Post Composition");
    compFieldPanel = new FieldPanel(false,false, null, this.selectionManager, this.editManager, null); // (searchParams)?
    //compFieldPanel.setSearchParams(searchParams);
    
    // MAIN GENUS TERM
    genusField = CharFieldGui.makePostCompTermList(charField,"Genus",minCompChars);
    genusField.setSelectionManager(this.selectionManager);
    //genusField.setListSelectionModel(this.selectionModel);
    compFieldPanel.addCharFieldGuiToPanel(genusField);

    // REL-DIFF - put in 1 differentia
    addRelDiffGui();

    setGuiFromSelectedModel();

    // override FieldPanel preferred size which will set window size
    compFieldPanel.setPreferredSize(null);//new Dimension(700,160));
    dialog.add(compFieldPanel);
    addButtons();
    dialog.pack();
    dialog.setLocationRelativeTo(owner);
    dialog.setVisible(true);

    compCharChangeListener = new CompCharChangeListener();
    this.editManager.addCharChangeListener(compCharChangeListener);
    compCharSelectListener = new CompCharSelectListener();
    this.selectionModel.addListSelectionListener(compCharSelectListener);
  }

  private void addRelDiffGui() {
    relDiffGuis.add(new RelDiffGui());
    dialog.pack();
  }

  /** RelDiffGui INNER CLASS */
  private class RelDiffGui {
    private CharFieldGui relField;
    // with embedded/recurse this will be a TermGui...
    private CharFieldGui diffField;
    private RelDiffGui() {
      CharField relChar = new CharField(CharFieldEnum.RELATIONSHIP);
      Ontology o = charField.getPostCompRelOntol();
      relChar.addOntology(o);
      relField = CharFieldGui.makeRelationList(relChar);//"Relationship"?
      relField.setMinCompChars(minCompChars);
      relField.setEditManager(PostCompGui.this.editManager);
      relField.setSelectionManager(PostCompGui.this.selectionManager);
      //relField.setListSelectionModel(PostCompGui.this.selectionModel);
      compFieldPanel.addCharFieldGuiToPanel(relField);
      diffField = CharFieldGui.makePostCompTermList(charField,"Differentia",minCompChars);
      diffField.setEditManager(PostCompGui.this.editManager);
      diffField.setSelectionManager(PostCompGui.this.selectionManager);
      //diffField.setListSelectionModel(PostCompGui.this.selectionModel);
      compFieldPanel.addCharFieldGuiToPanel(diffField);
    }
    private void setRelDiffModel(RelDiffModel rd) {
//       try { rd.relField.setRel(getRel(currentTerm)); } catch (Exception e){}
      relField.setRel(rd.rel);
      diffField.setOboClass(rd.diff);
      diffField.setOntologyChooserFromTerm(rd.diff);
    }
  } // end of RelDiffGui INNER CLASS


  private class RelDiffModel {
    private OBOProperty rel;
    private OBOClass diff;
    private RelDiffModel(OBORestriction link) {
      rel = link.getType();
      diff = (OBOClass)link.getParent();
    }
  }


  private void setGuiFromSelectedModel() {
    OBOClass currentTerm = getModelTerm();
    if (currentTerm == null) return;
    
    OBOClass genus = OboUtil.getGenusTerm(currentTerm);
    genusField.setOboClass(genus);
    // should this happen automatically from setOboClass or is that a burden/inefficiency
    genusField.setOntologyChooserFromTerm(genus);
    //if (modelHasDiff(currentTerm)) // should query if temp or real post comp??
    
    try {
      List<RelDiffModel>diffs = getRelDiffs(currentTerm); // CompEx if none
      
      for (int i=0; i<diffs.size(); i++) {
        // check that have enough guis for diffs
        if (i >= relDiffGuis.size()) addRelDiffGui();
        relDiffGuis.get(i).setRelDiffModel(diffs.get(i));
      }
    }
    //just got no diffs no need for msg log().debug("get rel diffs failed "+e); }
    catch (CompEx e) {}

  }

  /** If model has changed (main window fiddling - hmmm) set gui */
  private class CompCharChangeListener implements CharChangeListener {
    public void charChanged(CharChangeEvent e) {
      setGuiFromSelectedModel();
    }
  }

  private class CompCharSelectListener implements ListSelectionListener {

    public void valueChanged(ListSelectionEvent e) {
      setGuiFromSelectedModel();
    }
    
  }

  private OBOClass getModelTerm() {
    // there should be convenience method for this
    // multi select get 1st??
    //CharacterI c = this.selectionManager.getFirstSelectedCharacter();
    // TODO - this may not be the best
    if (!this.selectionModel.getSelected().isEmpty()) {
      CharacterI c = this.selectionModel.getSelected().get(0);
      return c.getValue(charField).getOboClass();
    } else {
      return null;
    }    
  }

  // util fn?
//   private boolean isPostCompTerm(OBOClass term) {
//     for (Object o : term.getParents()) {
//       if ( ((OBORestriction)o).completes() )
//         return true;
//     }
//     return false;
//   }

  //private String getGenusStr(OBOClass term){return getGenusTerm(term).getName();}

  /** for non post comp returns term itself */
//   private OBOClass getGenusTerm(OBOClass term) {
//     if (isPostCompTerm(term)) {
//       for (Object o : term.getParents()) {
//         OBORestriction r = (OBORestriction)o;
//         if (r.completes() && r.getType().equals(OBOProperty.IS_A))
//           return (OBOClass)r.getParent(); // check downcast?
//       }
//       // error msg?
//     }
//     return term;
//   }

  private class CompEx extends Exception {
    private CompEx() {}
    private CompEx(String m) { super(m); }
  }

  private List<RelDiffModel> getRelDiffs(OBOClass term) throws CompEx {
    if (!OboUtil.isPostCompTerm(term)) throw new CompEx();
    List<RelDiffModel>diffs = new ArrayList<RelDiffModel>(4);
    for (Link l : term.getParents()) {
      OBORestriction r = (OBORestriction)l;
      if (isLinkToDiff(r)) {
        diffs.add(new RelDiffModel(r));
      }
    }
    if (diffs.isEmpty()) throw new CompEx(); // none found
    return diffs;
  }

  private boolean isLinkToDiff(OBORestriction r) {
    return r.completes() && !r.getType().equals(OBOProperty.IS_A);
  }

//   /** Throws exception if no diff term - for now only returning one diff term
//       can there be more than one */
//   private OBOClass getDiffTerm(OBOClass term) throws CompEx {
//     OBORestriction link = getDiffLink(term); // throws Ex
//     return (OBOClass)link.getParent(); // check downcast?
//   }

//   /** If term is post comp return obo property relationship for differentia 
//       otherwise thorws exception */
//   private OBOProperty getRel(OBOClass term) throws CompEx {
//     return getDiffLink(term).getType(); // throws ex
//   }

//   /** return the oboRestriction link between term and its differentia 
//       exception if there is none. 
//       This assumes that there is only one diff - no longer true! 
//       should make specific ex*/
//   private OBORestriction getDiffLink(OBOClass term) throws CompEx {
//     if (!OboUtil.isPostCompTerm(term)) throw new CompEx();
//     for (Object o : term.getParents()) {
//       OBORestriction r = (OBORestriction)o;
//       if (r.completes() && r.getType() != OBOProperty.IS_A)
//         return r;
//     }
//     throw new CompEx(); // none found
//   }

  private void addButtons() {
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
    buttonPanel.add(Box.createHorizontalGlue());
    JButton addDiff = new JButton(new PlusAction());
    buttonPanel.add(addDiff);
    buttonPanel.add(Box.createRigidArea(new Dimension(140,50)));
    JButton ok = new JButton(new OkAction());
    buttonPanel.add(ok);
    buttonPanel.add(Box.createRigidArea(new Dimension(15,0)));
    JButton cancel = new JButton(new CancelAction());
    buttonPanel.add(cancel);
    buttonPanel.add(Box.createHorizontalGlue());
    dialog.add(buttonPanel,BorderLayout.SOUTH);
  }

  private class PlusAction extends AbstractAction {
    private PlusAction() { super("+"); }
    public void actionPerformed(ActionEvent e) { addRelDiffGui(); }
  }

  private class CancelAction extends AbstractAction {
    private CancelAction() { super("Cancel"); }
    public void actionPerformed(ActionEvent e) {
      //dialog.close();
      dispose();
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
      dispose(); // keep up if failure?
    }
  }

  private void dispose() {
    // this does not get rid of PostCompGui as connected object
    dialog.dispose();
    editManager.removeCharChangeListener(compCharChangeListener);
    selectionModel.removeListSelectionListener(compCharSelectListener);
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
    final List<CharacterI> chrs = this.selectionModel.getSelected();
    if (!chrs.isEmpty()) {
      final CompoundTransaction ct = CompoundTransaction.makeUpdate(chrs,charField,postComp);
      this.editManager.updateModel(this,ct);
    }
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
