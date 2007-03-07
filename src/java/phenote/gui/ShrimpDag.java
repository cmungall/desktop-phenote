package phenote.gui;


import java.awt.Component;
import javax.swing.JFrame;

import org.geneontology.oboedit.datamodel.OBOSession;

//import ca.uvic.csr.shrimp.gui.QueryView.QueryView;
import ca.uvic.csr.shrimp.gui.QueryView.OBOViewer;
import ca.uvic.csr.shrimp.gui.QueryView.QueryView;

import phenote.gui.selection.SelectionManager;
import phenote.gui.selection.TermSelectionEvent;
import phenote.gui.selection.TermSelectionListener;

import phenote.datamodel.Ontology;
import phenote.datamodel.OntologyManager;

class ShrimpDag {

  //private JFrame window;
  private OBOViewer oboViewer;
  private static ShrimpDag singleton;
	
  public static ShrimpDag inst() {
    if (singleton == null) singleton = new ShrimpDag();
    return singleton;
  }
  
  private ShrimpDag() {
    //window = new JFrame("Shrimp ontology viewer");
    //window.pack();
    //window.setVisible(true);
    init();
  }

  //public void display() {}
  
  /** send obo sessions to shrimp so it can set them up in its datamodel*/
  public void initOntologies() {
    // do this in a separate thread! no need to hold up phenote with this
    for (Ontology o : OntologyManager.inst().getAllOntologies()) {
      // oboViewer.loadOboSession(o.getOboSession());
    }
  }
  
  private void init() {
    //initShrimp();
    SelectionManager.inst().addTermSelectionListener(new ShrimpSelectionListener());
    //initOntologies();
  }
  public void display() {
	    
    //queryView = new QueryView(); false - show query view?
    boolean showQueryPanel = true;
    oboViewer = new OBOViewer(showQueryPanel);
    //new OBOViewer("Shrimp ontology viewer",getOboSession(),showQueryPanel);
    oboViewer.loadOBOSession(getOboSession()); // change this!

    JFrame frame = new JFrame("Shrimp DAG view");
    //frame.setDefaultCloseOperation(java.awt.WindowListener.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(oboViewer.getView());
    frame.pack();
    frame.setSize(600, 600);
    frame.setLocation(400, 200);
    frame.setVisible(true);
    
  }
  
  private OBOSession getOboSession() {
	// just hard wire to go for now
    try {
      Ontology o = OntologyManager.inst().getOntologyForName("ZF");
      // fly causes an endless loop - oh my
      //Ontology o = OntologyManager.inst().getOntologyForName("Fly");
      return o.getOboSession();
    } catch (phenote.datamodel.OntologyException e) {
      System.out.println("no ontol for dag");
      return null;
    }
  }
  
  private QueryView getQueryView() {
    return oboViewer.getQueryView();
  }
  
  private class ShrimpSelectionListener implements TermSelectionListener {

    public boolean termSelected(TermSelectionEvent e) {
      if (e.isMouseOverEvent()) return false;
      String term = e.getOboClass().getName();
      boolean animate = true;
      //getQueryView().query(term, animate);
      oboViewer.query(getOboSession(),e.getOboClass(),animate);
      return true;
    }
    
  }

}
